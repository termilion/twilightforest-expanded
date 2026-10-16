package twilightforest.entity.passive;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import twilightforest.entity.passive.dialogue.DialogueManager;

/**
 * Port of the Sporeling from Mobs of Mythology (Pixel Dream Studios, GPL-3.0), AzureLib → GeckoLib. A small walking mushroom
 * (red or brown) of the mushroom forest that panics when hurt and answers with a random line when a player clicks it:
 * red ones are cheerful, brown ones grumpy (lines from {@link DialogueManager}, mob {@code shroomling}, keys {@code red}/{@code brown}/{@code greeting}). Passive (creature), unlike the original monster-category spawn.
 */
public class Shroomling extends PathfinderMob implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation BOUNCE = RawAnimation.begin().thenPlay("bounce");
	private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Shroomling.class, EntityDataSerializers.INT);
	private static final int LINE_COOLDOWN = 60;

	private static final String DIALOGUE_MOB = "shroomling";

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private int lineCooldown;
	private boolean touched;
	@Nullable
	private String lastLine;

	public Shroomling(EntityType<? extends Shroomling> type, Level level) {
		super(type, level);
		this.xpReward = 1;
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 6.0D)
			.add(Attributes.MOVEMENT_SPEED, 0.3D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 0.8D));
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.75D));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
		this.setVariant(Util.getRandom(Variant.values(), this.getRandom()));
		return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_VARIANT, 0);
	}

	public Variant getVariant() {
		return Variant.byId(this.getEntityData().get(DATA_VARIANT));
	}

	private void setVariant(Variant variant) {
		this.getEntityData().set(DATA_VARIANT, variant.ordinal());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("Variant", this.getVariant().ordinal());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setVariant(Variant.byId(tag.getInt("Variant")));
	}

	// Persistent like animals: natural creature spawns are rare (every 400 ticks), so despawning ones would almost never be seen
	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.lineCooldown > 0)
			this.lineCooldown--;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (this.lineCooldown <= 0) {
			this.lineCooldown = LINE_COOLDOWN;
			// Runs on both sides: the client plays the bounce, the server talks
			this.touched = true;
			if (!this.level().isClientSide()) {
				boolean red = this.getVariant() == Variant.RED;
				String line = DialogueManager.INSTANCE.pickLine(player, DIALOGUE_MOB, red ? "red" : "brown", this.getRandom(), this.lastLine);
				if (line != null) {
					this.lastLine = line;
					this.playSound(red ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO, 1.0F, 2.0F);
					DialogueManager.INSTANCE.say(player, DIALOGUE_MOB, line, this.getRandom());
				}
			}
		}
		return super.mobInteract(player, hand);
	}

	// Tiny villager voice (pitched up), like the original
	@Override
	public float getVoicePitch() {
		return 2.0F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.VILLAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.VILLAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.VILLAGER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.WOLF_STEP, 0.25F, 2.0F);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 3, state -> state.setAndContinue(state.isMoving() ? WALK : IDLE)));
		controllers.add(new AnimationController<>(this, "bounceController", 3, state -> {
			if (this.touched) {
				state.getController().forceAnimationReset();
				state.getController().setAnimation(BOUNCE);
				this.touched = false;
			}
			return PlayState.CONTINUE;
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	public enum Variant {
		RED, BROWN;

		public static Variant byId(int id) {
			return values()[Math.floorMod(id, values().length)];
		}
	}
}
