package twilightforest.entity.passive.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the lines of talking mobs from {@code data/twilightforest/dialogue.json}:
 * {@code { "<language>": { "<mob>": { "<key>": ["line", ...] } } }}. Keys are biome ids, {@code default} or event names.
 * Lines are looked up in the player's client language (full code, then its prefix, e.g. {@code en_us} → {@code en}), falling back to {@code en}.
 * In a line, {@code {greeting}} stands for a random line of the mob's {@code greeting} key and {@code {player}} for the player's name.
 */
public class DialogueManager extends SimplePreparableReloadListener<Map<String, Map<String, Map<String, List<String>>>>> {
	public static final DialogueManager INSTANCE = new DialogueManager();
	private static final ResourceLocation FILE = TwilightForestMod.prefix("dialogue.json");
	private static final String FALLBACK_LANGUAGE = "en";

	private Map<String, Map<String, Map<String, List<String>>>> dialogue = Map.of();

	@Override
	protected Map<String, Map<String, Map<String, List<String>>>> prepare(ResourceManager manager, ProfilerFiller profiler) {
		Map<String, Map<String, Map<String, List<String>>>> result = new HashMap<>();
		Optional<Resource> resource = manager.getResource(FILE);
		if (resource.isEmpty()) {
			TwilightForestMod.LOGGER.warn("Dialogue file {} not found", FILE);
			return result;
		}
		try (Reader reader = resource.get().openAsReader()) {
			for (Map.Entry<String, JsonElement> language : JsonParser.parseReader(reader).getAsJsonObject().entrySet()) {
				Map<String, Map<String, List<String>>> mobs = new HashMap<>();
				for (Map.Entry<String, JsonElement> mob : language.getValue().getAsJsonObject().entrySet()) {
					Map<String, List<String>> keys = new HashMap<>();
					JsonObject mobLines = mob.getValue().getAsJsonObject();
					for (Map.Entry<String, JsonElement> key : mobLines.entrySet()) {
						List<String> lines = new ArrayList<>();
						// Lenient parsing turns a trailing comma into a null entry: skip it
						key.getValue().getAsJsonArray().forEach(line -> {
							if (!line.isJsonNull())
								lines.add(line.getAsString());
						});
						keys.put(key.getKey(), List.copyOf(lines));
					}
					mobs.put(mob.getKey(), keys);
				}
				result.put(language.getKey(), mobs);
			}
		} catch (Exception e) {
			TwilightForestMod.LOGGER.error("Failed to load dialogue file {}", FILE, e);
		}
		return result;
	}

	@Override
	protected void apply(Map<String, Map<String, Map<String, List<String>>>> prepared, ResourceManager manager, ProfilerFiller profiler) {
		this.dialogue = prepared;
	}

	/**
	 * @return the lines of {@code mob} for {@code key} in the player's language, or an empty list.
	 */
	public List<String> getLines(Player player, String mob, String key) {
		String language = player instanceof ServerPlayer serverPlayer ? serverPlayer.clientInformation().language() : FALLBACK_LANGUAGE;
		for (String candidate : List.of(language, language.split("_")[0], FALLBACK_LANGUAGE)) {
			List<String> lines = this.dialogue.getOrDefault(candidate, Map.of()).getOrDefault(mob, Map.of()).get(key);
			if (lines != null && !lines.isEmpty())
				return lines;
		}
		return List.of();
	}

	/**
	 * @return a random line of {@code mob} for {@code key}, if possible not {@code lastLine}, or null if there are none.
	 * Unresolved, so the caller can keep it as its next {@code lastLine}; see {@link #say}.
	 */
	@Nullable
	public String pickLine(Player player, String mob, String key, RandomSource random, @Nullable String lastLine) {
		List<String> lines = this.getLines(player, mob, key);
		if (lines.isEmpty())
			return null;
		String line;
		do {
			line = lines.get(random.nextInt(lines.size()));
		} while (line.equals(lastLine) && lines.size() > 1);
		return line;
	}

	// Shows a line, placeholders resolved, in the player's action bar
	public void say(Player player, String mob, String line, RandomSource random) {
		player.displayClientMessage(Component.literal(this.resolve(player, mob, line, random)), true);
	}

	private String resolve(Player player, String mob, String line, RandomSource random) {
		if (line.contains("{greeting}")) {
			List<String> greetings = this.getLines(player, mob, "greeting");
			line = line.replace("{greeting}", greetings.isEmpty() ? "" : greetings.get(random.nextInt(greetings.size())));
		}
		return line.replace("{player}", player.getScoreboardName());
	}
}
