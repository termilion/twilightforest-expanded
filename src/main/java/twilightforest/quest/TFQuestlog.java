package twilightforest.quest;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.EditorMetadata;
import org.infernalstudios.questlog.core.quests.QuestObjectiveRegistry;
import org.infernalstudios.questlog.event.events.QuestEvent;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFAdvancements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Hooks this mod into Questlog (a required dependency): our objective types, the quest files and the progress keys the quests unlock.
 * Questlog only reads quests from {@code config/questlog/}, so the files below {@code questlog/} in this jar are copied there on every launch.
 */
public final class TFQuestlog {
	private static final String RESOURCE_DIR = "questlog";

	public static void register(IEventBus modBus) {
		modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
			registerObjectives();
			installQuestFiles();
		}));
		NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) ->
			// Questlog drops all of its listeners when a server stops, so they have to be added again for every server
			Questlog.EVENTS.addListener(QuestEvent.Completed.class, TFQuestlog::onQuestCompleted));
		NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
			QuestRamStudies.clear();
			AllayFreedListeners.clear();
		});
		AllayIntro.register();
		CagedAllay.register();
	}

	private static void registerObjectives() {
		QuestObjectiveRegistry.register(TwilightForestMod.prefix("allay_freed"), AllayFreedObjective::new, new EditorMetadata(null, null, "required_amount"));
		QuestObjectiveRegistry.register(TwilightForestMod.prefix("entity_interact"), EntityInteractObjective::new,
			new EditorMetadata("entity", "Entity ID:", "required_amount", EditorMetadata.SuggestionType.ENTITY_TYPE));
	}

	/**
	 * Triggers a quest for a player the same way {@code /questlog trigger} does: by completing its requirements.
	 */
	public static void triggerQuest(ServerPlayer player, ResourceLocation id) {
		if (ServerPlayerManager.INSTANCE == null) return;
		QuestManager manager = ServerPlayerManager.INSTANCE.getManagerByPlayer(player);
		Quest quest = manager.getQuest(id);
		if (quest == null || quest.isTriggered()) return;
		quest.prerequisites.forEach(requirement -> requirement.forceSetUnits(requirement.getRequiredAmount()));
		// Completing the last requirement already synced the quest and sent the "triggered" event that shows the popup;
		// a full resync on top of that would replace the client's quest objects the popup refers to
		ServerPlayerManager.INSTANCE.save(manager);
	}

	private static void onQuestCompleted(QuestEvent.Completed event) {
		if (event.isServer && event.player instanceof ServerPlayer player) {
			TFAdvancements.QUEST_COMPLETED.get().trigger(player, event.quest.getId());
		}
	}

	/**
	 * Replaces {@code config/questlog/quests/twilightforest/} and the chapter file with the ones in this jar,
	 * so quest updates ship with the mod. Quests of other mods and packs, and the rest of the folder, are not touched.
	 */
	private static void installQuestFiles() {
		Path source = ModList.get().getModFileById(TwilightForestMod.ID).getFile().findResource(RESOURCE_DIR);
		Path questlogConfig = FMLPaths.CONFIGDIR.get().resolve("questlog");
		Path questTarget = questlogConfig.resolve("quests").resolve(TFQuests.FOLDER);
		Path chapterTarget = questlogConfig.resolve("chapters").resolve(TFQuests.FOLDER + ".json");

		try {
			if (Files.isDirectory(questTarget)) {
				try (Stream<Path> old = Files.walk(questTarget)) {
					for (Path path : (Iterable<Path>) old.sorted(Comparator.reverseOrder())::iterator) {
						Files.delete(path);
					}
				}
			}
			Files.createDirectories(questTarget);
			Files.createDirectories(chapterTarget.getParent());

			Path quests = source.resolve("quests");
			if (Files.isDirectory(quests)) {
				try (Stream<Path> files = Files.list(quests)) {
					for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".json"))::iterator) {
						Files.copy(file, questTarget.resolve(file.getFileName().toString()));
					}
				}
			}
			Path chapter = source.resolve("chapter.json");
			if (Files.exists(chapter)) {
				Files.copy(chapter, chapterTarget, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			TwilightForestMod.LOGGER.error("Failed to install the Twilight Forest quests into {}", questlogConfig, e);
		}
	}

	private TFQuestlog() {
	}
}
