package twilightforest.palemonarch.entity;

import twilightforest.palemonarch.procedures.IfPathfindIsTrueProcedure;
import twilightforest.palemonarch.procedures.KingAiNewProcedure;
import twilightforest.palemonarch.procedures.PaleMonarchEntityDiesProcedure;
import twilightforest.palemonarch.procedures.TiredLookProcedure;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.animation.AnimationController.State;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class PaleMonarchEntity extends Monster implements GeoEntity, SyncedAnimationEntity {
   public static final EntityDataAccessor<Boolean> SHOOT = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.STRING);
   public static final EntityDataAccessor<Integer> DATA_cooldown = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_attackcooldown = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_PathfindCooldown = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_strafedirection = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Boolean> DATA_IsBuffed = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Integer> DATA_AttacksUntilBuff = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_multiswingchanceup = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_AttacksUntilShards = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Boolean> DATA_canrangeduse = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Integer> DATA_dashcooldown = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Boolean> DATA_isNearAggravator = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Integer> DATA_clonecooldown = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> DATA_downtimebeforeheal = SynchedEntityData.defineId(PaleMonarchEntity.class, EntityDataSerializers.INT);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private boolean swinging;
   private boolean lastloop;
   private long lastSwing;
   public String animationprocedure = "empty";
   private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), BossBarColor.WHITE, BossBarOverlay.PROGRESS);
   String prevAnim = "empty";

   public PaleMonarchEntity(EntityType<PaleMonarchEntity> type, Level world) {
      super(type, world);
      this.xpReward = 0;
      this.setNoAi(false);
      this.setPersistenceRequired();
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(SHOOT, false);
      builder.define(ANIMATION, "undefined");
      builder.define(TEXTURE, "kingtexturestreamlined");
      builder.define(DATA_cooldown, 0);
      builder.define(DATA_attackcooldown, 0);
      builder.define(DATA_PathfindCooldown, 0);
      builder.define(DATA_strafedirection, 0);
      builder.define(DATA_IsBuffed, false);
      builder.define(DATA_AttacksUntilBuff, 0);
      builder.define(DATA_multiswingchanceup, 1);
      builder.define(DATA_AttacksUntilShards, 0);
      builder.define(DATA_canrangeduse, true);
      builder.define(DATA_dashcooldown, 0);
      builder.define(DATA_isNearAggravator, false);
      builder.define(DATA_clonecooldown, 0);
      builder.define(DATA_downtimebeforeheal, 0);
   }

   public void setTexture(String texture) {
      this.entityData.set(TEXTURE, texture);
   }

   public String getTexture() {
      return (String)this.entityData.get(TEXTURE);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
      this.goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0) {
         public boolean canUse() {
            double x = PaleMonarchEntity.this.getX();
            double y = PaleMonarchEntity.this.getY();
            double z = PaleMonarchEntity.this.getZ();
            Entity entity = PaleMonarchEntity.this;
            Level world = PaleMonarchEntity.this.level();
            return super.canUse() && IfPathfindIsTrueProcedure.execute(entity);
         }
      });
      this.targetSelector.addGoal(3, new HurtByTargetGoal(this, new Class[0]));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this) {
         public boolean canUse() {
            double x = PaleMonarchEntity.this.getX();
            double y = PaleMonarchEntity.this.getY();
            double z = PaleMonarchEntity.this.getZ();
            Entity entity = PaleMonarchEntity.this;
            Level world = PaleMonarchEntity.this.level();
            return super.canUse() && TiredLookProcedure.execute(entity);
         }
      });
      this.goalSelector.addGoal(5, new FloatGoal(this));
   }

   public boolean removeWhenFarAway(double distanceToClosestPlayer) {
      return false;
   }

   public SoundEvent getHurtSound(DamageSource ds) {
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.hurt"));
   }

   public SoundEvent getDeathSound() {
      return (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.death"));
   }

   public boolean hurt(DamageSource source, float amount) {
      if (source.getDirectEntity() instanceof AbstractArrow) {
         return false;
      } else if (source.is(DamageTypes.FALL)) {
         return false;
      } else {
         return source.is(DamageTypes.DROWN) ? false : super.hurt(source, amount);
      }
   }

   public void die(DamageSource source) {
      super.die(source);
      PaleMonarchEntityDiesProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
   }

   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString("Texture", this.getTexture());
      compound.putInt("Datacooldown", (Integer)this.entityData.get(DATA_cooldown));
      compound.putInt("Dataattackcooldown", (Integer)this.entityData.get(DATA_attackcooldown));
      compound.putInt("DataPathfindCooldown", (Integer)this.entityData.get(DATA_PathfindCooldown));
      compound.putInt("Datastrafedirection", (Integer)this.entityData.get(DATA_strafedirection));
      compound.putBoolean("DataIsBuffed", (Boolean)this.entityData.get(DATA_IsBuffed));
      compound.putInt("DataAttacksUntilBuff", (Integer)this.entityData.get(DATA_AttacksUntilBuff));
      compound.putInt("Datamultiswingchanceup", (Integer)this.entityData.get(DATA_multiswingchanceup));
      compound.putInt("DataAttacksUntilShards", (Integer)this.entityData.get(DATA_AttacksUntilShards));
      compound.putBoolean("Datacanrangeduse", (Boolean)this.entityData.get(DATA_canrangeduse));
      compound.putInt("Datadashcooldown", (Integer)this.entityData.get(DATA_dashcooldown));
      compound.putBoolean("DataisNearAggravator", (Boolean)this.entityData.get(DATA_isNearAggravator));
      compound.putInt("Dataclonecooldown", (Integer)this.entityData.get(DATA_clonecooldown));
      compound.putInt("Datadowntimebeforeheal", (Integer)this.entityData.get(DATA_downtimebeforeheal));
   }

   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains("Texture")) {
         this.setTexture(compound.getString("Texture"));
      }

      if (compound.contains("Datacooldown")) {
         this.entityData.set(DATA_cooldown, compound.getInt("Datacooldown"));
      }

      if (compound.contains("Dataattackcooldown")) {
         this.entityData.set(DATA_attackcooldown, compound.getInt("Dataattackcooldown"));
      }

      if (compound.contains("DataPathfindCooldown")) {
         this.entityData.set(DATA_PathfindCooldown, compound.getInt("DataPathfindCooldown"));
      }

      if (compound.contains("Datastrafedirection")) {
         this.entityData.set(DATA_strafedirection, compound.getInt("Datastrafedirection"));
      }

      if (compound.contains("DataIsBuffed")) {
         this.entityData.set(DATA_IsBuffed, compound.getBoolean("DataIsBuffed"));
      }

      if (compound.contains("DataAttacksUntilBuff")) {
         this.entityData.set(DATA_AttacksUntilBuff, compound.getInt("DataAttacksUntilBuff"));
      }

      if (compound.contains("Datamultiswingchanceup")) {
         this.entityData.set(DATA_multiswingchanceup, compound.getInt("Datamultiswingchanceup"));
      }

      if (compound.contains("DataAttacksUntilShards")) {
         this.entityData.set(DATA_AttacksUntilShards, compound.getInt("DataAttacksUntilShards"));
      }

      if (compound.contains("Datacanrangeduse")) {
         this.entityData.set(DATA_canrangeduse, compound.getBoolean("Datacanrangeduse"));
      }

      if (compound.contains("Datadashcooldown")) {
         this.entityData.set(DATA_dashcooldown, compound.getInt("Datadashcooldown"));
      }

      if (compound.contains("DataisNearAggravator")) {
         this.entityData.set(DATA_isNearAggravator, compound.getBoolean("DataisNearAggravator"));
      }

      if (compound.contains("Dataclonecooldown")) {
         this.entityData.set(DATA_clonecooldown, compound.getInt("Dataclonecooldown"));
      }

      if (compound.contains("Datadowntimebeforeheal")) {
         this.entityData.set(DATA_downtimebeforeheal, compound.getInt("Datadowntimebeforeheal"));
      }
   }

   public void baseTick() {
      super.baseTick();
      KingAiNewProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ(), this);
      this.refreshDimensions();
   }

   public EntityDimensions getDefaultDimensions(Pose pose) {
      return super.getDefaultDimensions(pose).scale(1.0F);
   }

   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossInfo.addPlayer(player);
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossInfo.removePlayer(player);
   }

   public void customServerAiStep() {
      super.customServerAiStep();
      this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
   }

   public static void init(RegisterSpawnPlacementsEvent event) {
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
      net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder builder = Mob.createMobAttributes();
      builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
      builder = builder.add(Attributes.MAX_HEALTH, 400.0);
      builder = builder.add(Attributes.ARMOR, 10.0);
      builder = builder.add(Attributes.ATTACK_DAMAGE, 3.0);
      builder = builder.add(Attributes.FOLLOW_RANGE, 32.0);
      builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
      return builder.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }

   private PlayState movementPredicate(AnimationState event) {
      if (this.animationprocedure.equals("empty")) {
         return this.isShiftKeyDown()
            ? event.setAndContinue(RawAnimation.begin().thenLoop("VERYslowwalk"))
            : event.setAndContinue(RawAnimation.begin().thenLoop("Idle"));
      } else {
         return PlayState.STOP;
      }
   }

   private PlayState procedurePredicate(AnimationState event) {
      if (!this.animationprocedure.equals("empty") && event.getController().getAnimationState() == State.STOPPED
         || !this.animationprocedure.equals(this.prevAnim) && !this.animationprocedure.equals("empty")) {
         if (!this.animationprocedure.equals(this.prevAnim)) {
            event.getController().forceAnimationReset();
         }

         event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
         if (event.getController().getAnimationState() == State.STOPPED) {
            this.animationprocedure = "empty";
            event.getController().forceAnimationReset();
         }
      } else if (this.animationprocedure.equals("empty")) {
         this.prevAnim = "empty";
         return PlayState.STOP;
      }

      this.prevAnim = this.animationprocedure;
      return PlayState.CONTINUE;
   }

   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 25) {
         this.remove(RemovalReason.KILLED);
         this.dropExperience(this);
      }
   }

   public String getSyncedAnimation() {
      return (String)this.entityData.get(ANIMATION);
   }

   public void setAnimation(String animation) {
      this.entityData.set(ANIMATION, animation);
   }

   public void playAnimation(String animation) {
      this.animationprocedure = animation;
   }

   public void registerControllers(ControllerRegistrar data) {
      data.add(new AnimationController(this, "movement", 0, this::movementPredicate));
      data.add(new AnimationController(this, "procedure", 0, this::procedurePredicate));
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
