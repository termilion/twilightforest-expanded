package twilightforest.quest;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.infernalstudios.questlog.core.QuestManager;
import org.infernalstudios.questlog.core.ServerPlayerManager;
import org.infernalstudios.questlog.core.quests.Quest;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.QuestAllay;
import twilightforest.init.TFDimension;
import twilightforest.init.TFEntities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Greets a player who enters the twilight forest without any progress with a quest allay, see {@link QuestAllay#startIntro}.
 * The allay triggers the first quest. Players who already have progress never get the first quest or the allay.
 */
public final class AllayIntro {
	// Gives the player a moment to arrive, and Questlog to load their quests when they log in directly into the forest
	private static final int DELAY_TICKS = 40;
	private static final List<net.minecraft.resources.ResourceLocation> STARTED_QUESTS = List.of(TFQuests.FIND_THE_RAM, TFQuests.LICH, TFQuests.HYDRA, TFQuests.UR_GHAST, TFQuests.AURORA, TFQuests.PALE_MONARCH);
	private static final Map<UUID, Integer> PENDING = new HashMap<>();

	public static void register() {
		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
			if (event.getEntity() instanceof ServerPlayer player && event.getTo().equals(TFDimension.DIMENSION_KEY)) {
				PENDING.put(player.getUUID(), DELAY_TICKS);
			}
		});
		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
			if (event.getEntity() instanceof ServerPlayer player && player.level().dimension().equals(TFDimension.DIMENSION_KEY)) {
				PENDING.put(player.getUUID(), DELAY_TICKS);
			}
		});
		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> PENDING.remove(event.getEntity().getUUID()));
		NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> PENDING.clear());
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
			if (PENDING.isEmpty()) return;
			for (Iterator<Map.Entry<UUID, Integer>> iterator = PENDING.entrySet().iterator(); iterator.hasNext(); ) {
				Map.Entry<UUID, Integer> entry = iterator.next();
				if (entry.setValue(entry.getValue() - 1) > 1) continue;
				iterator.remove();
				ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
				if (player != null) greet(player);
			}
		});
	}

	private static void greet(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(TFDimension.DIMENSION_KEY) || !player.isAlive()) return;
		QuestManager manager = ServerPlayerManager.INSTANCE == null ? null : ServerPlayerManager.INSTANCE.getManagerIfPresent(player);
		Quest first = manager == null ? null : manager.getQuest(TFQuests.FIND_THE_RAM);
		if (first == null || first.isTriggered()) return;

		// Players with progress already get no first quest; the lich quest unlocks from their progress key instead
		if (!hasProgress(player, manager) && level.getEntitiesOfClass(QuestAllay.class, new AABB(player.blockPosition()).inflate(64.0D), allay -> allay.isIntroFor(player)).isEmpty()) {
			QuestAllay allay = TFEntities.QUEST_ALLAY.get().create(level);
			if (allay != null) {
				allay.startIntro(player);
				level.addFreshEntity(allay);
			}
		}
	}

	// Any progression key, or any of the first six quests already started
	private static boolean hasProgress(ServerPlayer player, QuestManager manager) {
		for (var quest : STARTED_QUESTS) {
			Quest started = manager.getQuest(quest);
			if (started != null && started.isTriggered()) return true;
		}
		for (AdvancementHolder advancement : player.server.getAdvancements().getAllAdvancements()) {
			if (advancement.id().getNamespace().equals(TwilightForestMod.ID) && advancement.id().getPath().startsWith("progress_")
				&& player.getAdvancements().getOrStartProgress(advancement).isDone()) {
				return true;
			}
		}
		return false;
	}

	public static void triggerFirstQuest(ServerPlayer player) {
		TFQuestlog.triggerQuest(player, TFQuests.FIND_THE_RAM);
	}

	private AllayIntro() {
	}
}
