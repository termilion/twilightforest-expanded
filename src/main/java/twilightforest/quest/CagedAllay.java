package twilightforest.quest;

import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.dialogue.DialogueManager;
import twilightforest.init.TFItems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * The quest "Caged Allay": a player who reaches an allay that generated in a pillager outpost or a woodland mansion triggers the quest.
 * Speaking to that allay makes it tell its story ({@code freed_allay} in the dialogue file), then it drops a Twilight Shard and a Twilight Pool Map
 * to the nearest {@code twilightforest:portal_unlit}, and the quest's objective completes.
 */
public final class CagedAllay {
	private static final String DIALOGUE_MOB = "freed_allay";
	private static final String FREED_TAG = "twilightforest:freed_allay";
	private static final ResourceKey<Structure> PORTAL_UNLIT = ResourceKey.create(Registries.STRUCTURE, TwilightForestMod.prefix("portal_unlit"));
	private static final List<ResourceKey<Structure>> HOMES = List.of(
		ResourceKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace("pillager_outpost")),
		ResourceKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace("mansion")));
	private static final int LINES = 4;
	private static final int LINE_TICKS = 80;
	private static final int REWARD_TICK = LINES * LINE_TICKS - 20;
	private static final int TRIGGER_RANGE = 16;
	private static final int MAP_SEARCH_RADIUS = 100;
	private static final int MAP_COLOR = 0x9A5FD0;
	private static final List<Conversation> CONVERSATIONS = new ArrayList<>();

	private static final class Conversation {
		final Allay allay;
		final ServerPlayer player;
		int tick;

		Conversation(Allay allay, ServerPlayer player) {
			this.allay = allay;
			this.player = player;
		}
	}

	public static void register() {
		NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
			if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 40 == 0 && player.isAlive()) {
				checkReached(player);
			}
		});
		NeoForge.EVENT_BUS.addListener(CagedAllay::onInteract);
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> tickConversations());
		NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
			// A frozen allay is saved with its NoAI flag
			CONVERSATIONS.forEach(conversation -> conversation.allay.setNoAi(false));
			CONVERSATIONS.clear();
		});
	}

	private static boolean isInHome(ServerLevel level, BlockPos pos) {
		for (ResourceKey<Structure> key : HOMES) {
			Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(key);
			if (structure != null && level.structureManager().getStructureWithPieceAt(pos, structure).isValid()) return true;
		}
		return false;
	}

	private static boolean isCagedAllay(ServerLevel level, Allay allay) {
		return allay.isAlive() && !allay.getPersistentData().getBoolean(FREED_TAG) && isInHome(level, allay.blockPosition());
	}

	private static void checkReached(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel level) || !isInHome(level, player.blockPosition())) return;
		if (!level.getEntitiesOfClass(Allay.class, player.getBoundingBox().inflate(TRIGGER_RANGE), allay -> isCagedAllay(level, allay)).isEmpty()) {
			TFQuestlog.triggerQuest(player, TFQuests.CAGED_ALLAY);
		}
	}

	private static void onInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getTarget() instanceof Allay allay)) return;
		if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;
		if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || !isCagedAllay(level, allay)) return;

		allay.getPersistentData().putBoolean(FREED_TAG, true);
		allay.setPersistenceRequired();
		TFQuestlog.triggerQuest(player, TFQuests.CAGED_ALLAY);
		allay.setNoAi(true);
		allay.setDeltaMovement(Vec3.ZERO);
		CONVERSATIONS.add(new Conversation(allay, player));
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}

	private static void tickConversations() {
		for (Iterator<Conversation> iterator = CONVERSATIONS.iterator(); iterator.hasNext(); ) {
			Conversation conversation = iterator.next();
			Allay allay = conversation.allay;
			ServerPlayer player = conversation.player;
			if (!allay.isAlive() || !player.isAlive() || player.hasDisconnected() || allay.level() != player.level() || allay.distanceToSqr(player) > 32 * 32) {
				// Let the next visit start over
				allay.getPersistentData().remove(FREED_TAG);
				allay.setNoAi(false);
				iterator.remove();
				continue;
			}

			// Without AI the look control does not run, so turn the allay by hand
			allay.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
			int tick = conversation.tick++;
			if (tick % LINE_TICKS == 0 && tick / LINE_TICKS < LINES) {
				say(allay, player, "free_" + (tick / LINE_TICKS + 1));
			}
			if (tick == REWARD_TICK) {
				reward((ServerLevel) allay.level(), allay, player);
				AllayFreedListeners.fire(player);
				allay.setNoAi(false);
				iterator.remove();
			}
		}
	}

	private static void say(Allay allay, ServerPlayer player, String key) {
		String line = DialogueManager.INSTANCE.pickLine(player, DIALOGUE_MOB, key, allay.getRandom(), null);
		if (line != null) {
			DialogueManager.INSTANCE.say(player, DIALOGUE_MOB, line, allay.getRandom());
			allay.playSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 1.0F, 1.0F + allay.getRandom().nextFloat() * 0.4F);
		}
	}

	private static void reward(ServerLevel level, Allay allay, ServerPlayer player) {
		drop(level, allay, player, new ItemStack(TFItems.TWILIGHT_SHARD.get()));
		ItemStack map = createMap(level, allay.blockPosition());
		if (map != null) {
			drop(level, allay, player, map);
		}
		level.sendParticles(ParticleTypes.END_ROD, allay.getX(), allay.getY() + 0.3D, allay.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.05D);
		level.playSound(null, allay.getX(), allay.getY(), allay.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.4F);
	}

	private static void drop(ServerLevel level, Allay allay, ServerPlayer player, ItemStack stack) {
		ItemEntity item = new ItemEntity(level, allay.getX(), allay.getY() + 0.3D, allay.getZ(), stack);
		item.setDeltaMovement(player.position().subtract(allay.position()).normalize().scale(0.15D).add(0.0D, 0.2D, 0.0D));
		item.setDefaultPickUpDelay();
		level.addFreshEntity(item);
	}

	// Same recipe as vanilla's treasure maps: a map of the nearest structure with a red X on it
	private static ItemStack createMap(ServerLevel level, BlockPos from) {
		Holder.Reference<Structure> structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(PORTAL_UNLIT).orElse(null);
		if (structure == null) return null;
		Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure), from, MAP_SEARCH_RADIUS, false);
		if (found == null) return null;

		BlockPos target = found.getFirst();
		ItemStack map = MapItem.create(level, target.getX(), target.getZ(), (byte) 2, true, true);
		MapItem.renderBiomePreviewMap(level, map);
		MapItemSavedData.addTargetDecoration(map, target, "+", MapDecorationTypes.RED_X);
		map.set(DataComponents.ITEM_NAME, Component.translatable("item.twilightforest.twilight_pool_map"));
		map.set(DataComponents.MAP_COLOR, new MapItemColor(MAP_COLOR));
		return map;
	}

	private CagedAllay() {
	}
}
