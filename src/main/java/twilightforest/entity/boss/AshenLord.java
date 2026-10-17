package twilightforest.entity.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.ai.goal.ashenlord.*;
import twilightforest.init.*;

/**
 * The Ashen Lord from Feywild (MIT, SaphieNyako): seasonal leaf attacks, leaf shield, whirlwind. Boss of the snow mountain summit.
 */
public class AshenLord extends BaseTFBoss {

	private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(AshenLord.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> CHANNEL_TYPE = SynchedEntityData.defineId(AshenLord.class, EntityDataSerializers.INT);

	private static final int DEATH_ANIMATION_LENGTH = 160;

	public final AnimationState IDLE_ANIMATION = new AnimationState();
	public final AnimationState WALKING_ANIMATION = new AnimationState();
	public final AnimationState CHANNEL_ANIMATION = new AnimationState();
	private int movingTicks;

	@Nullable
	private ForcedAbility forcedAbility;
	private boolean retaliationRequested;
	private int agitationStage;

	public AshenLord(EntityType<? extends AshenLord> type, Level level) {
		super(type, level);
		this.xpReward = 400;
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.15D)
			.add(Attributes.MAX_HEALTH, 300.0D)
			.add(Attributes.FOLLOW_RANGE, 32.0D)
			.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
			.add(Attributes.ATTACK_DAMAGE, 12.0D)
			.add(Attributes.ARMOR_TOUGHNESS, 2.0D)
			.add(Attributes.ARMOR, 5.0D)
			.add(Attributes.STEP_HEIGHT, 1.2D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STATE, 0);
		builder.define(CHANNEL_TYPE, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new AshenLordRetaliationGoal(this));
		this.goalSelector.addGoal(2, new AutumnLeafShieldGoal(this));
		this.goalSelector.addGoal(3, new SummerHurlLeavesGoal(this));
		this.goalSelector.addGoal(4, new WinterHurlLeavesGoal(this));
		this.goalSelector.addGoal(5, new SpringLeafWhirlwindGoal(this));
		this.addRestrictionGoals(this, this.goalSelector);
		this.goalSelector.addGoal(6, new MoveTowardsTargetGoal(this, 1.0D, 32.0F));
		this.goalSelector.addGoal(30, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(30, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean damaged = super.hurt(source, amount);
		if (damaged && !this.level().isClientSide() && source.getEntity() instanceof Player) {
			this.retaliationRequested = true;
		}
		return damaged;
	}

	public boolean hasRetaliationRequest() {
		return this.retaliationRequested;
	}

	public void consumeRetaliationRequest() {
		this.retaliationRequested = false;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.setupAnimationStates();
		}
	}

	private void setupAnimationStates() {
		boolean channel = this.getState() == State.CHANNEL;
		boolean moving = !channel && this.isActuallyMoving();
		this.CHANNEL_ANIMATION.animateWhen(channel, this.tickCount);
		this.WALKING_ANIMATION.animateWhen(moving, this.tickCount);
		this.IDLE_ANIMATION.animateWhen(!channel && !moving, this.tickCount);
	}

	private boolean isActuallyMoving() {
		if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D) {
			this.movingTicks = 5;
		} else {
			this.movingTicks = Math.max(0, this.movingTicks - 1);
		}
		return this.movingTicks > 0;
	}

	@Override
	@Nullable
	protected SoundEvent getAmbientSound() {
		State state = this.getState();
		return state == State.IDLE || state == State.WALKING ? TFSounds.ASHEN_LORD_AMBIENT.get() : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TFSounds.ASHEN_LORD_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TFSounds.ASHEN_LORD_DEATH.get();
	}

	// Same slow ascent as Feywild's boss death, on the TF death path (loot, conquered landmark, advancements stay as for every TF boss)
	@Override
	protected void tickDeath() {
		this.setNoGravity(true);
		this.setDeltaMovement(0.0D, 0.02D, 0.0D);
		if (!this.level().isClientSide()) {
			this.stopChanneling();
			if (this.deathTime > 80 && this.deathTime % 10 == 0) {
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, this.getSoundSource(), 0.3F, 1.0F);
			}
		}

		super.tickDeath();
	}

	@Override
	public boolean isDeathAnimationFinished() {
		return this.deathTime >= DEATH_ANIMATION_LENGTH;
	}

	@Override
	public void tickDeathAnimation() {
		this.setupAnimationStates();

		for (int i = 0; i < 6; i++) {
			this.level().addParticle(TFParticleType.AUTUMN_SPARKLE.get(),
				this.getX() + this.getRandom().nextGaussian() * 0.5D,
				this.getY() + 1.0D + this.getRandom().nextGaussian() * 0.5D,
				this.getZ() + this.getRandom().nextGaussian() * 0.5D,
				0.0D, 0.0D, 0.0D);
		}

		if (this.deathTime > 80 && this.deathTime % 10 == 0) {
			this.level().addParticle(ParticleTypes.EXPLOSION,
				this.getX() + this.getRandom().nextGaussian() * 0.4D,
				this.getY() + 1.0D + this.getRandom().nextGaussian() * 0.6D,
				this.getZ() + this.getRandom().nextGaussian() * 0.4D,
				0.0D, 0.0D, 0.0D);
		}
	}

	public State getState() {
		State[] states = State.values();
		return states[Mth.clamp(this.getEntityData().get(STATE), 0, states.length - 1)];
	}

	public void setState(State state) {
		this.getEntityData().set(STATE, state.ordinal());
	}

	public ChannelType getChannelType() {
		ChannelType[] types = ChannelType.values();
		return types[Mth.clamp(this.getEntityData().get(CHANNEL_TYPE), 0, types.length - 1)];
	}

	public void setChannelType(ChannelType channelType) {
		this.getEntityData().set(CHANNEL_TYPE, channelType.ordinal());
	}

	public void startChanneling(ChannelType channelType) {
		this.setChannelType(channelType);
		this.setState(State.CHANNEL);
	}

	public void stopChanneling() {
		this.setState(State.IDLE);
		this.setChannelType(ChannelType.NONE);
	}

	@Nullable
	public ForcedAbility getForcedAbility() {
		return this.forcedAbility;
	}

	public boolean isForcedAbility(ForcedAbility ability) {
		return this.forcedAbility == ability;
	}

	public void setForcedAbility(ForcedAbility ability) {
		this.forcedAbility = ability;
	}

	public void clearForcedAbility() {
		this.forcedAbility = null;
	}

	public int getAgitationStage() {
		return this.agitationStage;
	}

	public void increaseAgitationStage() {
		this.agitationStage = Math.min(this.agitationStage + 1, 2);
	}

	public void resetAgitationStage() {
		this.agitationStage = 0;
	}

	@Override
	public int getHomeRadius() {
		return 20;
	}

	@Override
	public ResourceKey<Structure> getHomeStructure() {
		return TFStructures.PEAK_SHRINE;
	}

	@Override
	public Block getDeathContainer(RandomSource random) {
		return TFBlocks.TWILIGHT_OAK_CHEST.get();
	}

	@Override
	public Block getBossSpawner() {
		return TFBlocks.ASHEN_LORD_BOSS_SPAWNER.get();
	}

	@Override
	public int getBossBarColor() {
		return 0xA33A1E;
	}

	public enum State {
		IDLE, WALKING, CHANNEL
	}

	public enum ChannelType {
		NONE, SPRING, SUMMER, AUTUMN, WINTER
	}

	public enum ForcedAbility {
		AUTUMN_SHIELD, SPRING_WHIRLWIND, SUMMER_LEAVES, WINTER_LEAVES
	}
}
