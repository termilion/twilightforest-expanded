package twilightforest.ivarsbosses;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Display.BlockDisplay;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled from Ivar's Bosses
public class FenrirEntity extends PathfinderMob implements GeoEntity {
   private static final int ACTION_IDLE = 0;
   private static final int ACTION_BITE = 1;
   private static final int ACTION_SLAM = 2;
   private static final int ACTION_BURST = 3;
   private static final int ACTION_ROAR = 4;
   private static final int BITE_COOLDOWN_TICKS = 20;
   private static final int SLAM_COOLDOWN_TICKS = 34;
   private static final int BURST_COOLDOWN_TICKS = 84;
   private static final int ROAR_COOLDOWN_TICKS = 120;
   private static final int GLOBAL_RECOVERY_TICKS = 6;
   private static final int BITE_DURATION_TICKS = 20;
   private static final int SLAM_DURATION_TICKS = 27;
   private static final int BURST_DURATION_TICKS = 120;
   private static final int ROAR_DURATION_TICKS = 40;
   private static final int CRYSTAL_PILLAR_RISE_TICKS = 12;
   private static final int CRYSTAL_PILLAR_HOLD_TICKS = 18;
   private static final int CRYSTAL_PILLAR_SINK_TICKS = 14;
   private static final int CRYSTAL_PILLAR_LIFETIME_TICKS = 44;
   private static final int MAX_ACTIVE_CRYSTAL_PILLARS = 14;
   private static final int CRYSTAL_SHARD_FLIGHT_TICKS = 18;
   private static final int MAX_ACTIVE_CRYSTAL_SHARDS = 12;
   private static final int BURST_SHARD_LAUNCH_TICKS = 20;
   private static final int BURST_PILLAR_LAUNCH_TICKS = 54;
   private static final double FENRIR_SAFE_PILLAR_RADIUS = 3.15;
   private static final EntityDataAccessor<Integer> DATA_ACTION = SynchedEntityData.defineId(FenrirEntity.class, EntityDataSerializers.INT);
   private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
   private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
   private static final RawAnimation ANIM_BITE = RawAnimation.begin().thenPlay("attack_1").thenLoop("idle");
   private static final RawAnimation ANIM_SLAM = RawAnimation.begin().thenPlay("attack_2").thenLoop("idle");
   private static final RawAnimation ANIM_BURST = RawAnimation.begin().thenPlay("attack_3").thenLoop("idle");
   private static final RawAnimation ANIM_DEATH = RawAnimation.begin().thenPlayAndHold("death");
   private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
   private final ServerBossEvent bossBar;
   private final List<FenrirEntity.CrystalPillarEffect> crystalPillars = new ArrayList<>();
   private final List<FenrirEntity.CrystalShardEffect> crystalShards = new ArrayList<>();
   private int actionTimer;
   private int actionDuration;
   private boolean actionHitApplied;
   private int biteCooldown;
   private int slamCooldown;
   private int burstCooldown;
   private int roarCooldown;
   private int globalCooldown;
   private int announcedPhase = -1;
   private int pathRecalcTimer;
   private boolean burstPillarsApplied;
   // TF: set for structure spawns; features (blackstone columns, spires) may generate over the spot after the structure was placed
   private boolean clearSpawnSpace;

   public FenrirEntity(EntityType<? extends FenrirEntity> entityType, Level level) {
      super(entityType, level);
      this.bossBar = new ServerBossEvent(Component.translatable("entity.twilightforest.fenrir"), BossBarColor.PINK, BossBarOverlay.PROGRESS);
      this.xpReward = 750;
   }

   public static Builder createAttributes() {
      return PathfinderMob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 460.0)
         .add(Attributes.ATTACK_DAMAGE, 10.0)
         .add(Attributes.FOLLOW_RANGE, 40.0)
         .add(Attributes.MOVEMENT_SPEED, 0.43)
         .add(Attributes.ARMOR, 12.0)
         .add(Attributes.ARMOR_TOUGHNESS, 6.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
         .add(Attributes.STEP_HEIGHT, 1.2);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FenrirEntity.FenrirCombatGoal(this));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.92));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector
         .addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true, entity -> entity instanceof Player player && this.isSurvivalPlayer(player)));
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_ACTION, 0);
   }

   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossBar.addPlayer(player);
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossBar.removePlayer(player);
   }

   public void remove(RemovalReason reason) {
      this.bossBar.removeAllPlayers();
      this.clearCrystalPillars();
      this.clearCrystalShards();
      super.remove(reason);
   }

   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
      if (spawnType == MobSpawnType.STRUCTURE) {
         this.clearSpawnSpace = true;
      }
      return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      if (this.clearSpawnSpace) {
         tag.putBoolean("ClearSpawnSpace", true);
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.clearSpawnSpace = tag.getBoolean("ClearSpawnSpace");
   }

   // TF: Fenrir breaks out of whatever generated inside its body after it was placed by a structure
   private void breakOutOfBlocks() {
      AABB box = this.getBoundingBox().deflate(0.01);
      for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
         BlockState state = this.level().getBlockState(pos);
         if (!state.isAir() && !state.getCollisionShape(this.level(), pos).isEmpty() && state.getDestroySpeed(this.level(), pos) >= 0.0F && this.level().getBlockEntity(pos) == null) {
            this.level().destroyBlock(pos, false, this);
         }
      }
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         if (this.clearSpawnSpace) {
            this.clearSpawnSpace = false;
            this.breakOutOfBlocks();
         }
         this.refreshCombatTarget();
         this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
         this.tickCooldowns();
         this.tickAction();
         this.tickCrystalPillars();
         this.tickCrystalShards();
         LivingEntity target = this.getTarget();
         if (target != null && target.isAlive() && !this.isDeadOrDying()) {
            this.faceTarget(target);
         }
      }
   }

   private void refreshCombatTarget() {
      LivingEntity currentTarget = this.getTarget();
      if (!this.isValidCombatTarget(currentTarget)) {
         this.setTarget(null);
         double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
         Player nearestPlayer = null;
         double nearestDistSq = followRange * followRange;

         for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(followRange))) {
            if (this.isValidCombatTarget(player)) {
               double distSq = this.horizDistSq(player);
               if (distSq < nearestDistSq) {
                  nearestPlayer = player;
                  nearestDistSq = distSq;
               }
            }
         }

         if (nearestPlayer != null) {
            this.setTarget(nearestPlayer);
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      boolean hurt = super.hurt(source, amount);
      if (hurt && !this.level().isClientSide) {
         if (source.getEntity() instanceof LivingEntity living && this.isValidCombatTarget(living)) {
            this.setTarget(living);
         }

         if (!this.isDeadOrDying()) {
            this.playFenrirSound((SoundEvent)IvarsBosses.FENRIR_HURT.get(), 1.45F, 0.94F + this.random.nextFloat() * 0.12F);
         }
      }

      return hurt;
   }

   public void die(DamageSource damageSource) {
      if (!this.level().isClientSide) {
         this.playFenrirSound((SoundEvent)IvarsBosses.FENRIR_DEATH.get(), 2.0F, 0.9F + this.random.nextFloat() * 0.08F);
      }

      super.die(damageSource);
   }

   public int getFenrirAction() {
      return (Integer)this.entityData.get(DATA_ACTION);
   }

   public void setFenrirAction(int action) {
      this.entityData.set(DATA_ACTION, action);
   }

   private void tickCooldowns() {
      if (this.globalCooldown > 0) {
         this.globalCooldown--;
      }

      if (this.biteCooldown > 0) {
         this.biteCooldown--;
      }

      if (this.slamCooldown > 0) {
         this.slamCooldown--;
      }

      if (this.burstCooldown > 0) {
         this.burstCooldown--;
      }

      if (this.roarCooldown > 0) {
         this.roarCooldown--;
      }

      if (this.pathRecalcTimer > 0) {
         this.pathRecalcTimer--;
      }
   }

   private void tickAction() {
      if (this.actionTimer > 0) {
         LivingEntity target = this.getTarget();
         int action = this.getFenrirAction();
         int elapsed = this.actionDuration - this.actionTimer;
         if (action != 4 && !this.isValidCombatTarget(target)) {
            this.cancelAction();
         } else {
            if (target != null && target.isAlive()) {
               this.getLookControl().setLookAt(target, 18.0F, 18.0F);
            }

            switch (action) {
               case 1:
                  this.getNavigation().stop();
                  this.setSprinting(false);
                  if (!this.actionHitApplied && elapsed < 8) {
                     this.controlledAttackStep(target, 0.18, 2.0);
                  } else {
                     this.stopHorizontalMovement();
                  }

                  if (!this.actionHitApplied && elapsed >= 8 && elapsed <= 14 && target != null) {
                     if (this.hitTarget(target, 3.25, 0.7, 80, 0)) {
                        this.actionHitApplied = true;
                     }
                  } else if (elapsed > 14) {
                     this.actionHitApplied = true;
                  }
                  break;
               case 2:
                  this.getNavigation().stop();
                  this.setSprinting(false);
                  if (elapsed < 10) {
                     this.controlledAttackStep(target, 0.13, 2.65);
                  } else {
                     this.stopHorizontalMovement();
                  }

                  if (!this.actionHitApplied && elapsed >= 12 && elapsed <= 18) {
                     this.actionHitApplied = true;
                     this.spawnSlamPillars(target);
                     this.spawnCrystalPulse(3.0, 24);
                  } else if (elapsed > 18) {
                     this.actionHitApplied = true;
                  }
                  break;
               case 3:
                  this.getNavigation().stop();
                  this.setSprinting(false);
                  if (target != null && elapsed < 28 && this.horizDistSq(target) > this.focusedHitReachSq(target, 5.0)) {
                     this.controlledAttackStep(target, 0.12, 4.25);
                  } else {
                     this.stopHorizontalMovement();
                  }

                  if (elapsed < 90 && elapsed % 10 == 0) {
                     this.spawnCrystalPulse(4.5, 30);
                  }

                  if (!this.actionHitApplied && elapsed >= 20) {
                     this.actionHitApplied = true;
                     this.launchCrystalShards(target);
                     this.spawnCrystalPulse(4.8, 36);
                  }

                  if (!this.burstPillarsApplied && elapsed >= 54) {
                     this.burstPillarsApplied = true;
                     this.spawnBurstPillars(target);
                     this.spawnCrystalPulse(6.0, 48);
                  }
                  break;
               case 4:
                  this.getNavigation().stop();
                  this.setSprinting(false);
                  this.stopHorizontalMovement();
                  if (elapsed == 0) {
                     this.playFenrirSound((SoundEvent)IvarsBosses.FENRIR_AWAKENING.get(), 1.6F, 0.9F + this.random.nextFloat() * 0.1F);
                     this.spawnCrystalPulse(2.0, 18);
                  }
            }

            if (--this.actionTimer <= 0) {
               this.finishAction(action);
            }
         }
      }
   }

   private void startAction(int action, int duration) {
      this.setFenrirAction(action);
      this.actionTimer = duration;
      this.actionDuration = duration;
      this.actionHitApplied = false;
      this.burstPillarsApplied = false;
      this.getNavigation().stop();
      if (action == 4) {
         this.roarCooldown = 120;
      }
   }

   private void finishAction(int previousAction) {
      this.setFenrirAction(0);
      this.actionTimer = 0;
      this.actionDuration = 0;
      this.actionHitApplied = false;
      this.burstPillarsApplied = false;
      this.setSprinting(false);
      this.globalCooldown = Math.max(this.globalCooldown, previousAction == 3 ? 12 : 6);
   }

   void cancelAction() {
      if (this.actionTimer > 0) {
         this.setFenrirAction(0);
         this.actionTimer = 0;
         this.actionDuration = 0;
         this.actionHitApplied = false;
         this.burstPillarsApplied = false;
         this.setSprinting(false);
      }
   }

   void resetCombatEngagement() {
      this.announcedPhase = -1;
      this.globalCooldown = 0;
      this.pathRecalcTimer = 0;
   }

   private void chooseCombatAction(LivingEntity target) {
      if (this.globalCooldown > 0) {
         this.chaseOrHold(target);
      } else {
         int phase = this.getCombatPhase();
         double distSq = this.horizDistSq(target);
         double reachSq = this.attackReachSq(target);
         if (phase > this.announcedPhase && this.roarCooldown <= 0 && distSq <= 36.0) {
            this.announcedPhase = phase;
            this.startAction(4, 40);
         } else if (phase == 1 && this.burstCooldown <= 0 && distSq <= 100.0 && this.random.nextInt(3) == 0) {
            this.startBurst();
         } else if (distSq <= reachSq) {
            if (phase == 1 && this.burstCooldown <= 0 && this.random.nextBoolean()) {
               this.startBurst();
            } else if (this.slamCooldown <= 0 && this.random.nextBoolean()) {
               this.startSlam();
            } else if (this.biteCooldown <= 0) {
               this.startBite();
            } else {
               this.getNavigation().stop();
            }
         } else if (distSq <= 16.0 && this.slamCooldown <= 0) {
            this.startSlam();
         } else {
            this.chaseTarget(target);
         }
      }
   }

   private void chaseOrHold(LivingEntity target) {
      if (this.horizDistSq(target) > this.attackReachSq(target)) {
         this.chaseTarget(target);
      } else {
         this.getNavigation().stop();
         this.setSprinting(false);
         this.stopHorizontalMovement();
      }
   }

   private void startBite() {
      this.biteCooldown = 20;
      this.startAction(1, 20);
   }

   private void startSlam() {
      this.slamCooldown = 34;
      this.startAction(2, 27);
   }

   private void startBurst() {
      this.burstCooldown = 84;
      this.startAction(3, 120);
   }

   private void chaseTarget(LivingEntity target) {
      int phase = this.getCombatPhase();
      double distSq = this.horizDistSq(target);
      this.faceTarget(target);
      if (this.hasLineOfSight(target) && distSq > this.attackReachSq(target)) {
         this.getNavigation().stop();
         double directSpeed = phase == 1 ? 0.52 : 0.42;
         if (distSq > 196.0) {
            directSpeed += 0.06;
         }

         this.controlledAttackStep(target, directSpeed, 2.35);
         this.setSprinting(phase == 1 || distSq > 49.0);
         this.pathRecalcTimer = 3;
      } else {
         double speed = phase == 1 ? 1.38 : 1.22;
         if (distSq > 196.0) {
            speed += 0.1;
         }

         if (distSq > 81.0) {
            speed += 0.06;
         }

         this.setSprinting(phase == 1 || distSq > 100.0);
         if (this.pathRecalcTimer <= 0) {
            if (!this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed)) {
               this.controlledAttackStep(target, phase == 1 ? 0.48 : 0.4, 2.35);
            }

            this.pathRecalcTimer = 5;
         }
      }
   }

   private void controlledAttackStep(LivingEntity target, double speed, double stopDistance) {
      if (target != null && target.isAlive()) {
         Vec3 delta = target.position().subtract(this.position());
         Vec3 flat = new Vec3(delta.x, 0.0, delta.z);
         if (!(flat.lengthSqr() <= stopDistance * stopDistance) && !(flat.lengthSqr() < 1.0E-4)) {
            Vec3 direction = flat.normalize();
            Vec3 movement = this.getDeltaMovement();
            this.setDeltaMovement(direction.x * speed, movement.y, direction.z * speed);
         } else {
            this.stopHorizontalMovement();
         }
      } else {
         this.stopHorizontalMovement();
      }
   }

   private void faceTarget(LivingEntity target) {
      double dx = target.getX() - this.getX();
      double dz = target.getZ() - this.getZ();
      if (!(dx * dx + dz * dz < 0.01)) {
         float yaw = (float)(Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
         this.setYRot(yaw);
         this.setYHeadRot(yaw);
         this.yBodyRot = yaw;
      }
   }

   private void stopHorizontalMovement() {
      Vec3 movement = this.getDeltaMovement();
      this.setDeltaMovement(0.0, movement.y, 0.0);
   }

   private boolean hitTarget(LivingEntity target, double radius, double knockback, int slowTicks, int slowAmplifier) {
      if (target != null && target.isAlive()) {
         if (this.horizDistSq(target) > this.focusedHitReachSq(target, radius)) {
            return false;
         } else if (Math.abs(this.getY() - target.getY()) > 5.0) {
            return false;
         } else {
            float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
            boolean hurt = this.dealBossHit(target, damage);
            if (hurt) {
               target.knockback(knockback, this.getX() - target.getX(), this.getZ() - target.getZ());
               target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, slowAmplifier, false, true));
            }

            return hurt;
         }
      } else {
         return false;
      }
   }

   private void areaStrike(double radius, double knockback, int slowTicks, int slowAmplifier) {
      for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
         if (this.isValidVictim(victim)
            && !(this.horizDistSq(victim) > radius * radius)
            && this.dealBossHit(victim, (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
            victim.knockback(knockback, this.getX() - victim.getX(), this.getZ() - victim.getZ());
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, slowAmplifier, false, true));
         }
      }
   }

   private void spawnSlamPillars(LivingEntity target) {
      if (this.level() instanceof ServerLevel serverLevel && target != null) {
         Vec3 center = target.position();
         this.spawnCrystalPillarField(serverLevel, center.x, center.y, center.z, 5, 1.35, 2.85, 3, 4, true);
      }
   }

   private void spawnBurstPillars(LivingEntity target) {
      if (this.level() instanceof ServerLevel serverLevel) {
         Vec3 var4 = this.position();
         this.spawnCrystalPillarField(serverLevel, var4.x, var4.y, var4.z, 8, 2.75, 4.95, 2, 5, false);
      }
   }

   private void spawnCrystalPillarField(
      ServerLevel serverLevel,
      double centerX,
      double centerY,
      double centerZ,
      int count,
      double minRadius,
      double maxRadius,
      int minHeight,
      int maxHeight,
      boolean includeCenter
   ) {
      if (this.crystalPillars.size() < 14) {
         if (includeCenter && this.spawnCrystalPillarAt(serverLevel, centerX, centerY, centerZ, maxHeight, 3.15)) {
            this.spawnCrystalPulse(1.6, 10);
         }

         double baseAngle = this.random.nextDouble() * Math.PI * 2.0;

         for (int i = 0; i < count && this.crystalPillars.size() < 14; i++) {
            double angle = baseAngle + (Math.PI * 2) * i / count + (this.random.nextDouble() - 0.5) * 0.55;
            double radius = minRadius + this.random.nextDouble() * (maxRadius - minRadius);
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            int height = minHeight + this.random.nextInt(Math.max(1, maxHeight - minHeight + 1));
            if (this.spawnCrystalPillarAt(serverLevel, x, centerY, z, height, 3.15)) {
               this.spawnCrystalPulse(1.1, 6);
            }
         }
      }
   }

   private boolean spawnCrystalPillarAt(ServerLevel serverLevel, double x, double y, double z, int desiredHeight, double safeRadius) {
      if (this.crystalPillars.size() >= 14) {
         return false;
      } else if (this.isWithinFenrirSafetyRadius(x, z, safeRadius)) {
         return false;
      } else {
         BlockPos base = this.findCrystalPillarBase(serverLevel, x, y, z, desiredHeight);
         if (base == null) {
            return false;
         } else {
            int height = this.getCrystalPillarHeight(serverLevel, base, desiredHeight);
            if (height <= 0) {
               return false;
            } else {
               double damageMultiplier = desiredHeight >= 5 ? 1.15 : 1.0;
               double knockback = desiredHeight >= 5 ? 1.1 : 0.85;
               this.crystalPillars.add(new FenrirEntity.CrystalPillarEffect(base, height, damageMultiplier, knockback));
               FenrirEntity.CrystalPillarEffect effect = this.crystalPillars.get(this.crystalPillars.size() - 1);
               if (!this.spawnCrystalPillarDisplays(serverLevel, effect)) {
                  this.discardCrystalPillar(effect);
                  this.crystalPillars.remove(effect);
                  return false;
               } else {
                  this.updateCrystalPillarPosition(effect);
                  serverLevel.playSound(null, base, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 0.85F, 0.72F + this.random.nextFloat() * 0.12F);
                  return true;
               }
            }
         }
      }
   }

   private void tickCrystalPillars() {
      if (this.level() instanceof ServerLevel serverLevel && !this.crystalPillars.isEmpty()) {
         Iterator<FenrirEntity.CrystalPillarEffect> iterator = this.crystalPillars.iterator();

         while (iterator.hasNext()) {
            FenrirEntity.CrystalPillarEffect effect = iterator.next();
            effect.age++;
            this.updateCrystalPillarPosition(effect);
            if (!effect.damageApplied && effect.age >= 12) {
               effect.damageApplied = true;
               this.damageCrystalPillar(serverLevel, effect);
            }

            if (effect.age >= 44) {
               this.discardCrystalPillar(effect);
               iterator.remove();
            }
         }
      }
   }

   private void clearCrystalPillars() {
      if (!this.crystalPillars.isEmpty()) {
         if (this.level() instanceof ServerLevel) {
            for (FenrirEntity.CrystalPillarEffect effect : this.crystalPillars) {
               this.discardCrystalPillar(effect);
            }
         }

         this.crystalPillars.clear();
      }
   }

   private void clearCrystalShards() {
      if (!this.crystalShards.isEmpty()) {
         if (this.level() instanceof ServerLevel) {
            for (FenrirEntity.CrystalShardEffect effect : this.crystalShards) {
               this.discardCrystalShard(effect);
            }
         }

         this.crystalShards.clear();
      }
   }

   private boolean spawnCrystalPillarDisplays(ServerLevel serverLevel, FenrirEntity.CrystalPillarEffect effect) {
      for (int offset = 0; offset < effect.height; offset++) {
         BlockDisplay display = this.createCrystalDisplay(serverLevel, Blocks.AMETHYST_BLOCK.defaultBlockState());
         if (display == null) {
            return false;
         }

         effect.parts.add(new FenrirEntity.CrystalPillarPart(display, (double)offset));
      }

      BlockDisplay bud = this.createCrystalDisplay(serverLevel, Blocks.LARGE_AMETHYST_BUD.defaultBlockState());
      if (bud == null) {
         return false;
      } else {
         effect.parts.add(new FenrirEntity.CrystalPillarPart(bud, (double)effect.height));
         return true;
      }
   }

   private BlockDisplay createCrystalDisplay(ServerLevel serverLevel, BlockState state) {
      BlockDisplay display = (BlockDisplay)EntityType.BLOCK_DISPLAY.create(serverLevel);
      if (display == null) {
         return null;
      } else {
         CompoundTag tag = new CompoundTag();
         tag.put("block_state", NbtUtils.writeBlockState(state));
         tag.put("brightness", this.createBrightnessTag(15, 15));
         tag.putFloat("view_range", 64.0F);
         tag.putFloat("shadow_radius", 0.0F);
         tag.putFloat("shadow_strength", 0.0F);
         tag.putFloat("width", 1.0F);
         tag.putFloat("height", 1.0F);
         tag.putInt("teleport_duration", 2);
         display.load(tag);
         display.setNoGravity(true);
         display.addTag(IvarsBosses.FENRIR_EFFECT_TAG);
         serverLevel.addFreshEntity(display);
         return display;
      }
   }

   private void updateCrystalPillarPosition(FenrirEntity.CrystalPillarEffect effect) {
      double hiddenOffset = -(effect.height + 1.65);
      double offset;
      if (effect.age <= 12) {
         double progress = this.clamp01(effect.age / 12.0);
         offset = hiddenOffset * (1.0 - this.easeOutCubic(progress));
      } else if (effect.age <= 30) {
         offset = 0.0;
      } else {
         double sinkAge = effect.age - 12 - 18;
         double progress = this.clamp01(sinkAge / 14.0);
         offset = hiddenOffset * this.easeInCubic(progress);
      }

      double x = effect.base.getX();
      double z = effect.base.getZ();

      for (FenrirEntity.CrystalPillarPart part : effect.parts) {
         if (!part.display().isRemoved()) {
            part.display().setPos(x, effect.base.getY() + part.finalYOffset() + offset, z);
         }
      }
   }

   private void damageCrystalPillar(ServerLevel serverLevel, FenrirEntity.CrystalPillarEffect effect) {
      double x = effect.base.getX() + 0.5;
      double y = effect.base.getY();
      double z = effect.base.getZ() + 0.5;
      double radius = 1.15 + effect.height * 0.08;
      AABB damageArea = new AABB(x - radius, y - 0.25, z - radius, x + radius, y + effect.height + 1.35, z + radius);

      for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, damageArea)) {
         if (this.isValidVictim(victim)) {
            double dx = victim.getX() - x;
            double dz = victim.getZ() - z;
            if (!(dx * dx + dz * dz > radius * radius)) {
               float damage = (float)(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * effect.damageMultiplier);
               if (this.dealBossHit(victim, damage)) {
                  victim.knockback(effect.knockback, x - victim.getX(), z - victim.getZ());
                  victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90, 0, false, true));
               }
            }
         }
      }

      serverLevel.playSound(null, effect.base, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.0F, 0.85F + this.random.nextFloat() * 0.18F);
      this.sendAmethystParticles(
         serverLevel, Blocks.AMETHYST_BLOCK.defaultBlockState(), x, y + effect.height * 0.5, z, 18, 0.45, effect.height * 0.22, 0.45, 0.05
      );
      this.sendAmethystParticles(serverLevel, Blocks.AMETHYST_CLUSTER.defaultBlockState(), x, y + effect.height + 0.35, z, 8, 0.22, 0.18, 0.22, 0.02);
   }

   private void discardCrystalPillar(FenrirEntity.CrystalPillarEffect effect) {
      for (FenrirEntity.CrystalPillarPart part : effect.parts) {
         part.display().discard();
      }

      effect.parts.clear();
   }

   private double easeOutCubic(double progress) {
      double inverted = 1.0 - progress;
      return 1.0 - inverted * inverted * inverted;
   }

   private double easeInCubic(double progress) {
      return progress * progress * progress;
   }

   private double clamp01(double value) {
      if (value < 0.0) {
         return 0.0;
      } else {
         return value > 1.0 ? 1.0 : value;
      }
   }

   private void launchCrystalShards(LivingEntity target) {
      if (this.level() instanceof ServerLevel serverLevel && target != null && this.isValidCombatTarget(target)) {
         int shardCount = this.getCombatPhase() == 1 ? 7 : 5;
         Vec3 origin = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.72, this.getZ());
         Vec3 targetPoint = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);

         for (int i = 0; i < shardCount && this.crystalShards.size() < 12; i++) {
            double startSpreadX = (this.random.nextDouble() - 0.5) * 1.25;
            double startSpreadY = this.random.nextDouble() * 0.35;
            double startSpreadZ = (this.random.nextDouble() - 0.5) * 1.25;
            Vec3 start = origin.add(startSpreadX, startSpreadY, startSpreadZ);
            double targetSpreadX = (this.random.nextDouble() - 0.5) * 0.85;
            double targetSpreadY = (this.random.nextDouble() - 0.5) * 0.55;
            double targetSpreadZ = (this.random.nextDouble() - 0.5) * 0.85;
            Vec3 shardTarget = targetPoint.add(targetSpreadX, targetSpreadY, targetSpreadZ);
            BlockDisplay display = this.createCrystalDisplay(serverLevel, Blocks.LARGE_AMETHYST_BUD.defaultBlockState());
            if (display != null) {
               display.setPos(start.x - 0.5, start.y - 0.5, start.z - 0.5);
               FenrirEntity.CrystalShardEffect effect = new FenrirEntity.CrystalShardEffect(
                  display,
                  start,
                  shardTarget,
                  18 + this.random.nextInt(4),
                  0.85 + this.random.nextDouble() * 0.2,
                  0.55 + this.random.nextDouble() * 0.2,
                  0.95 + this.random.nextDouble() * 0.55
               );
               this.crystalShards.add(effect);
            }
         }

         serverLevel.playSound(
            null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 0.95F, 1.05F + this.random.nextFloat() * 0.18F
         );
         this.spawnCrystalPulse(4.2, 28);
      }
   }

   private void tickCrystalShards() {
      if (this.level() instanceof ServerLevel serverLevel && !this.crystalShards.isEmpty()) {
         Iterator<FenrirEntity.CrystalShardEffect> iterator = this.crystalShards.iterator();

         while (iterator.hasNext()) {
            FenrirEntity.CrystalShardEffect effect = iterator.next();
            effect.age++;
            this.updateCrystalShardPosition(effect);
            if (effect.age % 2 == 0) {
               this.sendAmethystParticles(
                  serverLevel, Blocks.AMETHYST_BLOCK.defaultBlockState(), effect.currentX, effect.currentY, effect.currentZ, 2, 0.05, 0.05, 0.05, 0.0
               );
            }

            if (!effect.damageApplied && effect.age >= effect.duration) {
               effect.damageApplied = true;
               this.damageCrystalShard(serverLevel, effect);
               this.discardCrystalShard(effect);
               iterator.remove();
            }
         }
      }
   }

   private void updateCrystalShardPosition(FenrirEntity.CrystalShardEffect effect) {
      double progress = this.clamp01((double)effect.age / effect.duration);
      double eased = this.easeOutCubic(progress);
      double x = effect.start.x + (effect.target.x - effect.start.x) * eased;
      double y = effect.start.y + (effect.target.y - effect.start.y) * eased + Math.sin(progress * Math.PI) * effect.arcHeight;
      double z = effect.start.z + (effect.target.z - effect.start.z) * eased;
      effect.currentX = x;
      effect.currentY = y;
      effect.currentZ = z;
      if (!effect.display.isRemoved()) {
         effect.display.setPos(x - 0.5, y - 0.5, z - 0.5);
      }
   }

   private void damageCrystalShard(ServerLevel serverLevel, FenrirEntity.CrystalShardEffect effect) {
      double x = effect.target.x;
      double y = effect.target.y;
      double z = effect.target.z;
      double radius = 1.25;
      AABB damageArea = new AABB(x - radius, y - 0.9, z - radius, x + radius, y + 1.1, z + radius);

      for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, damageArea)) {
         if (this.isValidVictim(victim)) {
            double dx = victim.getX() - x;
            double dz = victim.getZ() - z;
            if (!(dx * dx + dz * dz > radius * radius)) {
               float damage = (float)(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * effect.damageMultiplier);
               if (this.dealBossHit(victim, damage)) {
                  victim.knockback(effect.knockback, x - victim.getX(), z - victim.getZ());
                  victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true));
               }
            }
         }
      }

      serverLevel.playSound(
         null,
         effect.target.x,
         effect.target.y,
         effect.target.z,
         SoundEvents.AMETHYST_BLOCK_CHIME,
         SoundSource.HOSTILE,
         1.0F,
         0.92F + this.random.nextFloat() * 0.16F
      );
      this.sendAmethystParticles(serverLevel, Blocks.AMETHYST_BLOCK.defaultBlockState(), x, y, z, 14, 0.35, 0.28, 0.35, 0.02);
      this.sendAmethystParticles(serverLevel, Blocks.AMETHYST_CLUSTER.defaultBlockState(), x, y + 0.2, z, 10, 0.22, 0.18, 0.22, 0.015);
   }

   private void discardCrystalShard(FenrirEntity.CrystalShardEffect effect) {
      if (!effect.display.isRemoved()) {
         effect.display.discard();
      }
   }

   private BlockPos findCrystalPillarBase(ServerLevel serverLevel, double x, double y, double z, int desiredHeight) {
      BlockPos origin = BlockPos.containing(x, y, z);
      int minY = Math.max(serverLevel.getMinBuildHeight() + 1, origin.getY() - 4);
      int maxY = Math.min(serverLevel.getMaxBuildHeight() - desiredHeight - 1, origin.getY() + 2);

      for (int scanY = maxY; scanY >= minY; scanY--) {
         BlockPos base = new BlockPos(origin.getX(), scanY, origin.getZ());
         BlockPos floor = base.below();
         if (serverLevel.getBlockState(floor).isFaceSturdy(serverLevel, floor, Direction.UP)
            && this.getCrystalPillarHeight(serverLevel, base, desiredHeight) > 0) {
            return base;
         }
      }

      return null;
   }

   private int getCrystalPillarHeight(ServerLevel serverLevel, BlockPos base, int maxHeight) {
      for (int height = maxHeight; height >= 1; height--) {
         boolean clear = true;

         for (int offset = 0; offset <= height; offset++) {
            BlockPos pos = base.above(offset);
            if (!serverLevel.getBlockState(pos).getCollisionShape(serverLevel, pos).isEmpty()) {
               clear = false;
               break;
            }
         }

         if (clear) {
            return height;
         }
      }

      return 0;
   }

   private void spawnCrystalPulse(double radius, int particles) {
      if (this.level() instanceof ServerLevel serverLevel) {
         this.sendAmethystParticles(
            serverLevel,
            Blocks.AMETHYST_BLOCK.defaultBlockState(),
            this.getX(),
            this.getY() + 0.95,
            this.getZ(),
            particles,
            radius * 0.18,
            0.45,
            radius * 0.18,
            0.02
         );
         this.sendAmethystParticles(
            serverLevel,
            Blocks.AMETHYST_CLUSTER.defaultBlockState(),
            this.getX(),
            this.getY() + 0.85,
            this.getZ(),
            Math.max(8, particles / 2),
            radius * 0.12,
            0.25,
            radius * 0.12,
            0.01
         );
      }
   }

   private void sendAmethystParticles(
      ServerLevel serverLevel, BlockState state, double x, double y, double z, int count, double spreadX, double spreadY, double spreadZ, double speed
   ) {
      serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y, z, count, spreadX, spreadY, spreadZ, speed);
   }

   private CompoundTag createBrightnessTag(int blockLight, int skyLight) {
      CompoundTag brightness = new CompoundTag();
      brightness.putInt("block", Math.max(0, Math.min(15, blockLight)));
      brightness.putInt("sky", Math.max(0, Math.min(15, skyLight)));
      return brightness;
   }

   private boolean isWithinFenrirSafetyRadius(double x, double z, double radius) {
      double dx = x - this.getX();
      double dz = z - this.getZ();
      return dx * dx + dz * dz < radius * radius;
   }

   private void playFenrirSound(SoundEvent sound, float volume, float pitch) {
      if (this.level() instanceof ServerLevel serverLevel && !this.isSilent()) {
         serverLevel.playSound(null, this.getX(), this.getY() + this.getBbHeight() * 0.55, this.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
      }
   }

   private boolean dealBossHit(LivingEntity target, float damage) {
      if (target instanceof Player player && !this.isSurvivalPlayer(player)) {
         return false;
      } else {
         target.invulnerableTime = 0;
         return target.hurt(this.level().damageSources().mobAttack(this), damage);
      }
   }

   private boolean isValidVictim(LivingEntity victim) {
      return victim != this && victim.isAlive() ? !(victim instanceof Player player && !this.isSurvivalPlayer(player)) : false;
   }

   private boolean isValidCombatTarget(LivingEntity target) {
      return target != null && target.isAlive() && !(target instanceof Player player && !this.isSurvivalPlayer(player));
   }

   private boolean isSurvivalPlayer(Player player) {
      return player instanceof ServerPlayer serverPlayer
         ? serverPlayer.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
         : !player.isCreative() && !player.isSpectator();
   }

   private int getCombatPhase() {
      float healthPct = this.getMaxHealth() <= 0.0F ? 1.0F : this.getHealth() / this.getMaxHealth();
      return healthPct <= 0.5F ? 1 : 0;
   }

   private double attackReachSq(LivingEntity target) {
      double reach = this.getBbWidth() * 0.5 + target.getBbWidth() + 1.2;
      return reach * reach;
   }

   private double focusedHitReachSq(LivingEntity target, double radius) {
      double bodyAwareRadius = this.getBbWidth() * 0.5 + target.getBbWidth() + 0.65;
      double reach = Math.max(radius, bodyAwareRadius);
      return reach * reach;
   }

   private double horizDistSq(LivingEntity target) {
      double dx = this.getX() - target.getX();
      double dz = this.getZ() - target.getZ();
      return dx * dx + dz * dz;
   }

   protected SoundEvent getAmbientSound() {
      return null;
   }

   protected SoundEvent getHurtSound(DamageSource ds) {
      return null;
   }

   protected SoundEvent getDeathSound() {
      return null;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController(this, "fenrir_controller", 2, this::animPredicate));
   }

   private <T extends GeoAnimatable> PlayState animPredicate(AnimationState<T> state) {
      if (this.isDeadOrDying()) {
         return state.setAndContinue(ANIM_DEATH);
      } else {
         return switch (this.getFenrirAction()) {
            case 1 -> state.setAndContinue(ANIM_BITE);
            case 2 -> state.setAndContinue(ANIM_SLAM);
            case 3 -> state.setAndContinue(ANIM_BURST);
            case 4 -> state.setAndContinue(ANIM_BURST);
            default -> this.getDeltaMovement().horizontalDistance() > 0.025 ? state.setAndContinue(ANIM_WALK) : state.setAndContinue(ANIM_IDLE);
         };
      }
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.animationCache;
   }

   private static final class CrystalPillarEffect {
      private final BlockPos base;
      private final int height;
      private final double damageMultiplier;
      private final double knockback;
      private final List<FenrirEntity.CrystalPillarPart> parts = new ArrayList<>();
      private int age;
      private boolean damageApplied;

      private CrystalPillarEffect(BlockPos base, int height, double damageMultiplier, double knockback) {
         this.base = base;
         this.height = height;
         this.damageMultiplier = damageMultiplier;
         this.knockback = knockback;
      }
   }

   private record CrystalPillarPart(BlockDisplay display, double finalYOffset) {
   }

   private static final class CrystalShardEffect {
      private final BlockDisplay display;
      private final Vec3 start;
      private final Vec3 target;
      private final int duration;
      private final double damageMultiplier;
      private final double knockback;
      private final double arcHeight;
      private int age;
      private boolean damageApplied;
      private double currentX;
      private double currentY;
      private double currentZ;

      private CrystalShardEffect(BlockDisplay display, Vec3 start, Vec3 target, int duration, double damageMultiplier, double knockback, double arcHeight) {
         this.display = display;
         this.start = start;
         this.target = target;
         this.duration = duration;
         this.damageMultiplier = damageMultiplier;
         this.knockback = knockback;
         this.arcHeight = arcHeight;
         this.currentX = start.x;
         this.currentY = start.y;
         this.currentZ = start.z;
      }
   }

   private static final class FenrirCombatGoal extends Goal {
      private final FenrirEntity fenrir;

      private FenrirCombatGoal(FenrirEntity fenrir) {
         this.fenrir = fenrir;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
      }

      public boolean canUse() {
         LivingEntity target = this.fenrir.getTarget();
         return this.fenrir.isValidCombatTarget(target);
      }

      public boolean canContinueToUse() {
         LivingEntity target = this.fenrir.getTarget();
         return this.fenrir.isValidCombatTarget(target);
      }

      public void start() {
         this.fenrir.resetCombatEngagement();
      }

      public void stop() {
         this.fenrir.setSprinting(false);
         this.fenrir.getNavigation().stop();
         this.fenrir.cancelAction();
         this.fenrir.resetCombatEngagement();
      }

      public void tick() {
         LivingEntity target = this.fenrir.getTarget();
         if (target != null && target.isAlive()) {
            this.fenrir.getLookControl().setLookAt(target, 18.0F, 18.0F);
            if (this.fenrir.actionTimer <= 0) {
               this.fenrir.chooseCombatAction(target);
            }
         }
      }
   }
}
