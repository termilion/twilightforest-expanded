package twilightforest.entity.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.data.tags.StructureTagGenerator;
import twilightforest.entity.passive.dialogue.DialogueManager;
import twilightforest.init.TFSounds;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Port of the Friends &amp; Foes rascal (Faboslav, CC BY-NC-ND 4.0): a small peaceful goblin with a sack that lingers around
 * Twilight Forest structures. Hit by a player, it vanishes in a puff of smoke instead of taking damage.
 * <p>
 * Clicked, it talks like the shroomling, with lines from {@link DialogueManager} (mob {@code brownie}): first the lines of the biome
 * it stands in, in order, then either a random {@code default} line or (30 %) the rascal's hide-and-seek game: it nods, vanishes,
 * hides invisibly nearby and has to be found (walk up to it) three times; then it hands over {@link #REWARD} and leaves for good.
 */
public class Brownie extends PathfinderMob {
	public static final int ACTION_NONE = 0;
	public static final int ACTION_NOD = 1;
	public static final int ACTION_GIVE_REWARD = 2;
	private static final EntityDataAccessor<Integer> DATA_ACTION = SynchedEntityData.defineId(Brownie.class, EntityDataSerializers.INT);
	private static final ResourceKey<LootTable> REWARD = ResourceKey.create(Registries.LOOT_TABLE, TwilightForestMod.prefix("rewards/brownie_hide_and_seek"));
	private static final String DIALOGUE_MOB = "brownie";
	private static final float GAME_CHANCE = 0.3F;
	private static final int LINE_COOLDOWN = 60;
	private static final int TALK_TICKS = 10;
	private static final int NOD_TICKS = 30;
	private static final int REWARD_GIVE_TICK = 22;
	private static final int REWARD_TICKS = 40;
	private static final int HIDE_TICKS = 200;
	private static final int ROUNDS = 3;
	private static final double FIND_DISTANCE = 5.0D;
	private static final double GIVE_UP_DISTANCE = 64.0D;
	private static final int GIVE_UP_TICKS = 3 * 60 * 20;

	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState nodAnimationState = new AnimationState();
	public final AnimationState giveRewardAnimationState = new AnimationState();

	private int lineCooldown;
	private int actionTicks;
	@Nullable
	private String dialogueBiome;
	private int dialogueIndex;
	@Nullable
	private String lastLine;
	// Hide-and-seek: the seeking player, how often it was found, ticks since the current round started
	@Nullable
	private UUID seeker;
	private int caughtCount;
	private int seekTicks;

	public Brownie(EntityType<? extends Brownie> type, Level level) {
		super(type, level);
		this.setPathfindingMalus(PathType.RAIL, 0.0F);
		this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
		this.setPathfindingMalus(PathType.WATER, 0.0F);
		this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 20.0D)
			.add(Attributes.MOVEMENT_SPEED, 0.55D)
			.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
	}

	/**
	 * Natural spawns only happen next to Twilight Forest structures: the chunk or one of its neighbours must be part of a structure
	 * in {@link StructureTagGenerator#BROWNIE_HAUNTS}. Chunk references are used directly, so this also works during chunk generation.
	 */
	public static boolean checkBrownieSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
		if (!Mob.checkMobSpawnRules(type, level, spawnType, pos, random))
			return false;
		if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION)
			return true;

		var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		ChunkPos center = new ChunkPos(pos);
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				ChunkAccess chunk = level.getChunk(center.x + dx, center.z + dz, ChunkStatus.STRUCTURE_REFERENCES, false);
				if (chunk == null)
					continue;
				for (Structure structure : chunk.getAllReferences().keySet())
					if (structures.wrapAsHolder(structure).is(StructureTagGenerator.BROWNIE_HAUNTS))
						return true;
			}
		}
		return false;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new StandStillGoal());
		this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 12.0F, 0.8D, 1.0D, player -> this.isHiding() && player.getUUID().equals(this.seeker)));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ACTION, ACTION_NONE);
	}

	public int getAction() {
		return this.getEntityData().get(DATA_ACTION);
	}

	// For the hide-and-seek minigame later on
	public void setAction(int action) {
		this.getEntityData().set(DATA_ACTION, action);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			int action = this.getAction();
			this.idleAnimationState.animateWhen(action == ACTION_NONE && !this.walkAnimation.isMoving(), this.tickCount);
			this.nodAnimationState.animateWhen(action == ACTION_NOD, this.tickCount);
			this.giveRewardAnimationState.animateWhen(action == ACTION_GIVE_REWARD, this.tickCount);
		} else {
			if (this.lineCooldown > 0)
				this.lineCooldown--;
			this.tickAction();
			if (this.seeker != null)
				this.tickHideAndSeek();
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND || this.seeker != null || this.getAction() != ACTION_NONE)
			return super.mobInteract(player, hand);
		if (!this.level().isClientSide() && this.lineCooldown <= 0) {
			this.lineCooldown = LINE_COOLDOWN;
			this.talk(player);
		}
		return InteractionResult.sidedSuccess(this.level().isClientSide());
	}

	// Biome lines in order (restarting when the brownie is in another biome), then a default line or the hide-and-seek game
	private void talk(Player player) {
		String biome = this.level().getBiome(this.blockPosition()).unwrapKey().map(ResourceKey::location).map(Object::toString).orElse("");
		if (!biome.equals(this.dialogueBiome)) {
			this.dialogueBiome = biome;
			this.dialogueIndex = 0;
		}

		List<String> biomeLines = DialogueManager.INSTANCE.getLines(player, DIALOGUE_MOB, biome);
		if (this.dialogueIndex < biomeLines.size()) {
			this.say(player, biomeLines.get(this.dialogueIndex++));
			this.startAction(ACTION_NOD, TALK_TICKS, player);
		} else if (this.getRandom().nextFloat() < GAME_CHANCE) {
			this.startHideAndSeek(player);
		} else {
			List<String> lines = DialogueManager.INSTANCE.getLines(player, DIALOGUE_MOB, "default");
			if (lines.isEmpty())
				return;
			String line;
			do {
				line = lines.get(this.getRandom().nextInt(lines.size()));
			} while (line.equals(this.lastLine) && lines.size() > 1);
			this.lastLine = line;
			this.say(player, line);
			this.startAction(ACTION_NOD, TALK_TICKS, player);
		}
	}

	private void say(Player player, String line) {
		this.playSound(TFSounds.BROWNIE_AMBIENT.get(), this.getSoundVolume(), this.getVoicePitch());
		player.displayClientMessage(Component.literal(line), true);
	}

	// Says a random line of an event key (hide_and_seek_start/found/won) if the dialogue file has one
	private void sayEvent(Player player, String key) {
		List<String> lines = DialogueManager.INSTANCE.getLines(player, DIALOGUE_MOB, key);
		if (!lines.isEmpty())
			player.displayClientMessage(Component.literal(lines.get(this.getRandom().nextInt(lines.size()))), true);
	}

	private void startAction(int action, int ticks, Player lookAt) {
		this.setAction(action);
		this.actionTicks = ticks;
		this.getNavigation().stop();
		this.getLookControl().setLookAt(lookAt);
	}

	// Ends nods and the reward hand-over; a hide-and-seek nod ends with the brownie vanishing
	private void tickAction() {
		if (this.getAction() == ACTION_NONE)
			return;
		Player seeker = this.getSeeker();
		if (seeker != null)
			this.getLookControl().setLookAt(seeker);

		this.actionTicks--;
		if (this.getAction() == ACTION_GIVE_REWARD && this.actionTicks == REWARD_TICKS - REWARD_GIVE_TICK && seeker != null)
			this.giveReward(seeker);
		if (this.actionTicks > 0)
			return;

		int action = this.getAction();
		this.setAction(ACTION_NONE);
		if (action == ACTION_GIVE_REWARD) {
			this.vanish();
			this.discard();
		} else if (this.seeker != null) {
			this.hide(seeker);
		}
	}

	private void startHideAndSeek(Player player) {
		this.seeker = player.getUUID();
		this.caughtCount = 0;
		this.playSound(TFSounds.BROWNIE_NOD.get(), 1.0F, this.getVoicePitch());
		this.sayEvent(player, "hide_and_seek_start");
		this.startAction(ACTION_NOD, NOD_TICKS, player);
	}

	private void tickHideAndSeek() {
		Player seeker = this.getSeeker();
		this.seekTicks++;
		if (seeker == null || seeker.isSpectator() || this.distanceTo(seeker) > GIVE_UP_DISTANCE || this.seekTicks > GIVE_UP_TICKS) {
			this.stopHideAndSeek();
			return;
		}
		if (this.getAction() != ACTION_NONE || this.isHiding())
			return;

		// Visible again: found once the seeker walks up to it
		if (this.distanceTo(seeker) <= FIND_DISTANCE && this.hasLineOfSight(seeker)) {
			this.caughtCount++;
			if (this.caughtCount >= ROUNDS) {
				this.playSound(TFSounds.BROWNIE_REWARD.get(), 1.0F, this.getVoicePitch());
				this.sayEvent(seeker, "hide_and_seek_won");
				this.startAction(ACTION_GIVE_REWARD, REWARD_TICKS, seeker);
			} else {
				this.playSound(TFSounds.BROWNIE_NOD.get(), 1.0F, this.getVoicePitch());
				this.sayEvent(seeker, "hide_and_seek_found");
				this.startAction(ACTION_NOD, NOD_TICKS, seeker);
			}
		}
	}

	// The seeker left, died or gave up: the brownie shows itself and goes back to talking
	private void stopHideAndSeek() {
		this.seeker = null;
		this.caughtCount = 0;
		this.seekTicks = 0;
		if (this.hasEffect(MobEffects.INVISIBILITY)) {
			this.removeEffect(MobEffects.INVISIBILITY);
			this.playSound(TFSounds.BROWNIE_REAPPEAR.get(), 2.0F, 1.5F + this.getRandom().nextFloat() * 0.1F);
		}
	}

	private void hide(@Nullable Player seeker) {
		this.vanish();
		this.seekTicks = 0;
		this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, HIDE_TICKS, 0, false, false));
		if (seeker != null)
			this.teleportAway(seeker);
	}

	private void vanish() {
		this.playSound(TFSounds.BROWNIE_DISAPPEAR.get(), 2.0F, 1.5F + this.getRandom().nextFloat() * 0.1F);
		this.spawnParticles(ParticleTypes.CLOUD);
	}

	// Like the rascal: random spots up to 8 blocks away (±4 in height), first one more than 10 blocks from the seeker wins
	private void teleportAway(Player seeker) {
		Vec3 start = this.position();
		for (int i = 0; i < 64; i++) {
			double x = start.x() + (this.getRandom().nextDouble() - 0.5D) * 16.0D;
			double y = start.y() + this.getRandom().nextInt(8) - 4;
			double z = start.z() + (this.getRandom().nextDouble() - 0.5D) * 16.0D;
			if (this.randomTeleport(x, y, z, false) && this.distanceTo(seeker) > 10.0D)
				return;
		}
	}

	private void giveReward(Player seeker) {
		if (!(this.level() instanceof ServerLevel level))
			return;
		LootParams params = new LootParams.Builder(level)
			.withParameter(LootContextParams.ORIGIN, this.position())
			.withParameter(LootContextParams.THIS_ENTITY, seeker)
			.create(LootContextParamSets.GIFT);
		for (ItemStack stack : level.getServer().reloadableRegistries().getLootTable(REWARD).getRandomItems(params))
			BehaviorUtils.throwItem(this, stack, seeker.position().add(0.0D, 1.0D, 0.0D));
	}

	public boolean isHiding() {
		return this.seeker != null && this.hasEffect(MobEffects.INVISIBILITY);
	}

	@Nullable
	private Player getSeeker() {
		return this.seeker == null ? null : this.level().getPlayerByUUID(this.seeker);
	}

	@Override
	public void onEffectRemoved(MobEffectInstance effect) {
		super.onEffectRemoved(effect);
		if (this.seeker != null && effect.getEffect().is(MobEffects.INVISIBILITY))
			this.playSound(TFSounds.BROWNIE_REAPPEAR.get(), 2.0F, 1.5F + this.getRandom().nextFloat() * 0.1F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.dialogueBiome != null)
			tag.putString("DialogueBiome", this.dialogueBiome);
		tag.putInt("DialogueIndex", this.dialogueIndex);
		if (this.seeker != null) {
			tag.putUUID("Seeker", this.seeker);
			tag.putInt("CaughtCount", this.caughtCount);
			tag.putInt("SeekTicks", this.seekTicks);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.dialogueBiome = tag.contains("DialogueBiome") ? tag.getString("DialogueBiome") : null;
		this.dialogueIndex = tag.getInt("DialogueIndex");
		this.seeker = tag.hasUUID("Seeker") ? tag.getUUID("Seeker") : null;
		this.caughtCount = tag.getInt("CaughtCount");
		this.seekTicks = tag.getInt("SeekTicks");
	}

	@Override
	public void playAmbientSound() {
		if (!this.isHiding())
			super.playAmbientSound();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		if (!this.isHiding())
			super.playStepSound(pos, state);
	}

	// Keeps the brownie in place while it nods or hands over the reward
	private class StandStillGoal extends Goal {
		StandStillGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
		}

		@Override
		public boolean canUse() {
			return Brownie.this.getAction() != ACTION_NONE;
		}

		@Override
		public void start() {
			Brownie.this.getNavigation().stop();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}
	}

	// Like the rascal: a player's hit makes it vanish instead of hurting it (named brownies take damage normally)
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!(source.getEntity() instanceof Player) || this.hasCustomName() || this.level().isClientSide())
			return super.hurt(source, amount);

		this.playHurtSound(source);
		this.playSound(TFSounds.BROWNIE_DISAPPEAR.get(), 2.0F, 1.5F + this.getRandom().nextFloat() * 0.1F);
		this.spawnParticles(ParticleTypes.CLOUD);
		this.spawnParticles(ParticleTypes.ANGRY_VILLAGER);
		this.discard();
		return false;
	}

	private void spawnParticles(ParticleOptions particle) {
		if (this.level() instanceof ServerLevel level)
			level.sendParticles(particle, this.getX(), this.getY(0.5D), this.getZ(), 16, this.getBbWidth() / 2, this.getBbHeight() / 2, this.getBbWidth() / 2, 0.1D);
	}

	@Override
	public float getVoicePitch() {
		return 1.15F + this.getRandom().nextFloat() * 0.15F;
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TFSounds.BROWNIE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TFSounds.BROWNIE_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TFSounds.BROWNIE_HURT.get();
	}
}
