package twilightforest.entity.monster;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import twilightforest.entity.ai.goal.AvoidAnyEntityGoal;
import twilightforest.entity.ai.goal.MadDwarfLightTNTGoal;
import twilightforest.entity.ai.goal.MadDwarfShyGoal;
import twilightforest.init.TFSounds;

// A dwarf driven mad by the king's curse. The model is a GeckoLib port of The Dwarven Forge dwarf (MIT).
public class MadDwarf extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	private static final DustParticleOptions MADNESS_DUST = new DustParticleOptions(new Vector3f(0.62F, 0.18F, 1.0F), 0.9F);

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public ItemStack heldPick = new ItemStack(Items.IRON_PICKAXE);
	public final ItemStack heldTNT = new ItemStack(Blocks.TNT);
	public final ItemStack heldFlint = new ItemStack(Items.FLINT_AND_STEEL);

	public MadDwarf(EntityType<? extends MadDwarf> type, Level world) {
		super(type, world);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new AvoidAnyEntityGoal<>(this, PrimedTnt.class, 2.0F, 1.0F, 2.0F));
		this.goalSelector.addGoal(2, new MadDwarfShyGoal(this, 1.0F));
		this.goalSelector.addGoal(3, new MadDwarfLightTNTGoal(this, 1.0F)); // light TNT
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, false));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 20.0D)
			.add(Attributes.MOVEMENT_SPEED, 0.28D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TFSounds.MAD_DWARF_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TFSounds.MAD_DWARF_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TFSounds.MAD_DWARF_DEATH.get();
	}

	/** Selects the warrior skin; overridden by {@link MadDwarfWarrior}. */
	public boolean isWarrior() {
		return false;
	}

	/** Stable per-entity beard colour index (0-3), no syncing needed. */
	public int getBeardVariant() {
		return Math.floorMod(this.getUUID().hashCode(), 4);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide() && this.isAlive()) {
			this.spawnMadnessParticles();
		}
	}

	private void spawnMadnessParticles() {
		RandomSource random = this.getRandom();
		if (random.nextInt(3) == 0) {
			this.level().addParticle(ParticleTypes.WITCH, this.getRandomX(0.6D), this.getY() + random.nextDouble() * this.getBbHeight(), this.getRandomZ(0.6D), 0.0D, 0.02D, 0.0D);
		}
		if (random.nextInt(5) == 0) {
			this.level().addParticle(MADNESS_DUST, this.getRandomX(0.5D), this.getY() + 0.2D + random.nextDouble() * this.getBbHeight(), this.getRandomZ(0.5D), 0.0D, 0.03D, 0.0D);
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 0, state -> state.setAndContinue(IDLE)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	public boolean isShy() {
		return this.lastHurtByPlayerTime <= 0;
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor accessor, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
		SpawnGroupData data = super.finalizeSpawn(accessor, difficulty, reason, spawnDataIn);

		this.populateDefaultEquipmentSlots(accessor.getRandom(), difficulty);
		this.populateDefaultEquipmentEnchantments(accessor, accessor.getRandom(), difficulty);

		this.setDropChance(EquipmentSlot.MAINHAND, 0.2F);

		return data;
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource source, DifficultyInstance difficulty) {
		this.setItemSlot(EquipmentSlot.MAINHAND, this.heldPick);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("TNTLeft", this.heldTNT.getCount());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.heldTNT.setCount(compound.getInt("TNTLeft"));
	}
}
