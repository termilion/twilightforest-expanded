package twilightforest.entity.passive;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

import java.util.List;

/**
 * Port of the Sporeling from Mobs of Mythology (Pixel Dream Studios, GPL-3.0), AzureLib → GeckoLib. A small walking mushroom
 * (red or brown) of the mushroom forest that panics when hurt and answers with a random line when a player clicks it:
 * red ones are cheerful, brown ones grumpy. Passive (creature), unlike the original monster-category spawn.
 */
public class Shroomling extends PathfinderMob implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
	private static final RawAnimation BOUNCE = RawAnimation.begin().thenPlay("bounce");
	private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Shroomling.class, EntityDataSerializers.INT);
	private static final int LINE_COOLDOWN = 60;

	private static final List<String> RED_LINES = List.of(
		"playerGreeting",
		"Seen any smurfs lately?",
		"I hate Gargamel...",
		"Mondays, huh?",
		"You're a big fellow!",
		"Please don't eat me!",
		"Why did the Creeper hang out with me? Because I'm such a fun-gi to be around!",
		"They say I'm a fun-gi, but I think I'm just a cap-tivating conversationalist!",
		"Looking for a spore-tacular time? You've found the right mushroom!",
		"Did you hear about the fungi who threw a party? It was a real spore-gy!",
		"I'm not edibles!",
		"My cousin Brownie is an alchemist.",
		"Do you know of a bard named Smash Mouth? I hear he's an All Star.",
		"I'm not a regular mushroom; I'm a spore-tacular mushroom!",
		"Some people blame me for the apocalypse...",
		"Γειά σου Ελλάδα!");
	private static final List<String> BROWN_LINES = List.of(
		"Why do I feel like a fun-guy in a no-fun zone?",
		"You want a piece of my spore attitude? Take a hike!",
		"Life's a spore-t, and then you decompose.",
		"Step back, or I'll give you a spore-tacular scowl!",
		"Why do players always pick on mushrooms? Can't we just have a cap-py existence?",
		"I'd rather be left alone in my dark corner. No mushroom for company!",
		"I don't have mushroom for joy, just a whole lot of fung-titude!",
		"Have a fung-tastic day...",
		"I am responsible for the apocalypse.",
		"I'm not grumpy, I'm just a fungi with a perpetual frown.",
		"Why are you bothering me? Can't you see I'm having a spore day?",
		"Why do players keep trying to cheer me up? Do I look like a sunflower to you?",
		"I'm like a mushroom, growing in the darkness of my grumpy thoughts.",
		"Step lightly, or you might awaken the wrath of the grumpy mushroom!",
		"Why be a fun-guy when you can be a grump-guy?",
		"I've mastered the art of being perpetually annoyed. It's my spore-cialty!",
		"Let's get blazed! Go bring the blaze rod.",
		"Is it 4:20 already?");
	private static final List<String> GREETINGS = List.of("Hello there, ", "Hey there, ", "Howdy, ", "Howdy-do, ", "Salutations, ", "Hiya, ", "Godspeed, ");

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
				List<String> lines = red ? RED_LINES : BROWN_LINES;
				String line;
				do {
					line = lines.get(this.getRandom().nextInt(lines.size()));
				} while (line.equals(this.lastLine) && lines.size() > 1);
				this.lastLine = line;
				if (line.equals("playerGreeting"))
					line = GREETINGS.get(this.getRandom().nextInt(GREETINGS.size())) + player.getScoreboardName() + "!";
				this.playSound(red ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO, 1.0F, 2.0F);
				player.displayClientMessage(Component.literal(line), true);
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
