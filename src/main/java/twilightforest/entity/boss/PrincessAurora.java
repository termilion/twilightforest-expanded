package twilightforest.entity.boss;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.ai.goal.AuroraIntimidateGoal;
import twilightforest.entity.ai.goal.AuroraStrikeGoal;
import twilightforest.entity.ai.goal.AuroraSummonVexGoal;
import twilightforest.init.*;

/**
 * Fights like Queen Mab from Feywild (MIT, SaphieNyako): teleport strikes, intimidation, vex summons.
 */
public class PrincessAurora extends BaseTFBoss {

	private static final EntityDataAccessor<Byte> STATE = SynchedEntityData.defineId(PrincessAurora.class, EntityDataSerializers.BYTE);

	private static final int RETALIATION_WINDOW = 20 * 3;
	private static final int DEATH_ANIMATION_LENGTH = 160;

	public enum State {IDLE, CHANNEL, INTIMIDATION, ATTACKING}

	public final AnimationState flyingIdleAnimationState = new AnimationState();
	public final AnimationState flyingAnimationState = new AnimationState();
	public final AnimationState channelAnimationState = new AnimationState();
	public final AnimationState intimidationAnimationState = new AnimationState();
	public final AnimationState attackAnimationState = new AnimationState();
	private int movingTicks;

	@Nullable
	private Player recentAttacker;
	private int recentAttackerTicks;

	public PrincessAurora(EntityType<? extends PrincessAurora> type, Level level) {
		super(type, level);

		this.xpReward = 317;
		this.moveControl = new FlyingMoveControl(this, 4, true);
		this.setNoGravity(true);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(2, new AuroraStrikeGoal(this));
		this.goalSelector.addGoal(3, new AuroraIntimidateGoal(this));
		this.goalSelector.addGoal(4, new AuroraSummonVexGoal(this));
		this.addRestrictionGoals(this, this.goalSelector);
		this.goalSelector.addGoal(30, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(50, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.5D)
			.add(Attributes.FLYING_SPEED, 0.5D)
			.add(Attributes.ATTACK_DAMAGE, 6.0D)
			.add(Attributes.FOLLOW_RANGE, 40.0D)
			.add(Attributes.MAX_HEALTH, 300.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
		navigation.setCanOpenDoors(false);
		navigation.setCanFloat(true);
		navigation.setCanPassDoors(true);
		return navigation;
	}

	@Override
	public void travel(Vec3 input) {
		if (this.isInWater() || this.isInLava()) {
			this.moveRelative(0.02F, input);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(this.isInWater() ? 0.8D : 0.5D));
		} else {
			float friction = 0.91F;
			if (this.onGround()) {
				BlockPos ground = this.getBlockPosBelowThatAffectsMyMovement();
				friction = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
			}
			this.moveRelative(this.onGround() ? 0.1F * (0.16277137F / (friction * friction * friction)) : 0.02F, input);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(friction));
		}
		this.calculateEntityAnimation(false);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STATE, (byte) 0);
	}

	public State getState() {
		return State.values()[Mth.clamp(this.getEntityData().get(STATE), 0, State.values().length - 1)];
	}

	public void setState(State state) {
		this.getEntityData().set(STATE, (byte) state.ordinal());
	}

	@Override
	@Nullable
	protected SoundEvent getAmbientSound() {
		return this.getState() == State.IDLE ? TFSounds.PRINCESS_AURORA_AMBIENT.get() : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TFSounds.PRINCESS_AURORA_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TFSounds.PRINCESS_AURORA_DEATH.get();
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide()) {
			this.setupAnimationStates();
		} else {
			this.tickRecentAttacker();
		}
	}

	private void setupAnimationStates() {
		State state = this.getState();
		this.attackAnimationState.animateWhen(state == State.ATTACKING, this.tickCount);
		this.channelAnimationState.animateWhen(state == State.CHANNEL, this.tickCount);
		this.intimidationAnimationState.animateWhen(state == State.INTIMIDATION, this.tickCount);

		boolean moving = this.isActuallyMoving();
		this.flyingAnimationState.animateWhen(state == State.IDLE && moving, this.tickCount);
		this.flyingIdleAnimationState.animateWhen(state == State.IDLE && !moving, this.tickCount);
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
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() && !this.isDeadOrDying()) {
			// make snow particles
			for (int i = 0; i < 3; i++) {
				float px = (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F;
				float py = this.getEyeHeight() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5F;
				float pz = (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F;

				this.level().addParticle(TFParticleType.SNOW_GUARDIAN.get(), this.xOld + px, this.yOld + py, this.zOld + pz, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	private void tickRecentAttacker() {
		if (this.recentAttackerTicks > 0) {
			this.recentAttackerTicks--;
		}
		if (this.recentAttackerTicks <= 0 || this.recentAttacker == null || !this.recentAttacker.isAlive() || this.recentAttacker.isRemoved()) {
			this.recentAttacker = null;
			this.recentAttackerTicks = 0;
		}
	}

	/**
	 * The player who hit her in the last 3 seconds, she strikes back at them.
	 */
	@Nullable
	public Player getRecentAttacker() {
		return this.recentAttacker != null && this.recentAttackerTicks > 0 && this.recentAttacker.isAlive() ? this.recentAttacker : null;
	}

	@Override
	public boolean hurt(DamageSource source, float damage) {
		boolean result = super.hurt(source, damage);

		if (result && !this.level().isClientSide() && this.isAlive() && source.getEntity() instanceof Player player) {
			this.recentAttacker = player;
			this.recentAttackerTicks = RETALIATION_WINDOW;
			this.setTarget(player);
		}
		return result;
	}

	@Override
	protected void tickDeath() {
		// slowly float upwards while dying
		this.setDeltaMovement(0.0D, 0.02D, 0.0D);
		if (!this.level().isClientSide()) {
			this.setState(State.IDLE);
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
			this.level().addParticle(TFParticleType.SNOW.get(),
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

	@Override
	public int getHomeRadius() {
		return 20;
	}

	@Override
	public ResourceKey<Structure> getHomeStructure() {
		return TFStructures.AURORA_PALACE;
	}

	@Override
	public Block getDeathContainer(RandomSource random) {
		return TFBlocks.TWILIGHT_OAK_CHEST.get();
	}

	@Override
	public Block getBossSpawner() {
		return TFBlocks.PRINCESS_AURORA_BOSS_SPAWNER.get();
	}

	@Override
	public int getBossBarColor() {
		return 0x8CF0F0;
	}
}
