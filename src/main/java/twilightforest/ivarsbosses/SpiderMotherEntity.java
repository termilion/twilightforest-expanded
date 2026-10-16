package twilightforest.ivarsbosses;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
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
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
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
public class SpiderMotherEntity extends PathfinderMob implements GeoEntity {
   private static final int ACTION_IDLE = 0;
   private static final int ACTION_BITE = 1;
   private static final int ACTION_CLAW = 2;
   private static final int ACTION_SPIT = 3;
   private static final int ACTION_GRAPPLE = 4;
   private static final int ACTION_WEB = 5;
   private static final int ACTION_STUN_HIT = 6;
   private static final int ACTION_WEB_PULL = 7;
   private static final int BITE_COOLDOWN_TICKS = 30;
   private static final int CLAW_COOLDOWN_TICKS = 46;
   private static final int SPIT_COOLDOWN_TICKS = 72;
   private static final int GRAPPLE_COOLDOWN_TICKS = 120;
   private static final int WEB_COOLDOWN_TICKS = 110;
   private static final int WEB_PULL_COOLDOWN_TICKS = 150;
   private static final int MINION_SUMMON_COOLDOWN_TICKS = 2400;
   private static final int GLOBAL_RECOVERY_TICKS = 10;
   private static final int DIRECT_POISON_TICKS = 60;
   private static final float MINION_HEAL_AMOUNT = 8.0F;
   private static final String MINION_TAG = "twilightforest.spider_mother_minion";
   private static final String MINION_OWNER_TAG_PREFIX = "twilightforest.spider_mother_owner_";
   private static final int BITE_DURATION_TICKS = 37;
   private static final int CLAW_DURATION_TICKS = 30;
   private static final int SPIT_DURATION_TICKS = 40;
   private static final int GRAPPLE_DURATION_TICKS = 64;
   private static final int WEB_DURATION_TICKS = 70;
   private static final int STUN_HIT_DURATION_TICKS = 6;
   private static final int WEB_PULL_DURATION_TICKS = 44;
   private static final int GRAPPLE_HOLD_TICKS = 30;
   private static final double GRAPPLE_SHORT_START_RANGE = 11.0;
   private static final double GRAPPLE_MID_START_RANGE = 13.5;
   private static final double WEB_PULL_STOP_DISTANCE = 1.25;
   private static final double GRAPPLE_HOLD_STOP_DISTANCE = 0.45;
   private static final EntityDataAccessor<Integer> DATA_ACTION = SynchedEntityData.defineId(SpiderMotherEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_GRAPPLE_HOLDING = SynchedEntityData.defineId(SpiderMotherEntity.class, EntityDataSerializers.BOOLEAN);
   private static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");
   private static final RawAnimation ANIM_WALK = RawAnimation.begin().thenLoop("walk");
   private static final RawAnimation ANIM_IDLE_MISSING_1 = RawAnimation.begin().thenLoop("idle_missing_1");
   private static final RawAnimation ANIM_WALK_MISSING_1 = RawAnimation.begin().thenLoop("walk_missing_1");
   private static final RawAnimation ANIM_BITE = RawAnimation.begin().thenPlay("attack1").thenLoop("idle");
   private static final RawAnimation ANIM_CLAW = RawAnimation.begin().thenPlay("attack2").thenLoop("idle");
   private static final RawAnimation ANIM_SPIT = RawAnimation.begin().thenPlay("spit").thenLoop("idle");
   private static final RawAnimation ANIM_GRAPPLE = RawAnimation.begin().thenPlay("grapple").thenLoop("idle");
   private static final RawAnimation ANIM_GRAPPLE_HOLD = RawAnimation.begin().thenLoop("stun");
   private static final RawAnimation ANIM_WEB = RawAnimation.begin().thenPlay("web").thenLoop("idle");
   private static final RawAnimation ANIM_STUN_HIT = RawAnimation.begin().thenPlay("stun_hit").thenLoop("idle");
   private static final RawAnimation ANIM_DEATH = RawAnimation.begin().thenPlayAndHold("death");
   private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
   private final ServerBossEvent bossBar = new ServerBossEvent(
      Component.translatable("entity.twilightforest.spider_mother"), BossBarColor.PURPLE, BossBarOverlay.PROGRESS
   );
   private int actionTimer;
   private int actionDuration;
   private boolean actionHitApplied;
   private int globalCooldown;
   private int biteCooldown;
   private int clawCooldown;
   private int spitCooldown;
   private int grappleCooldown;
   private int webCooldown;
   private int webPullCooldown;
   private int minionSummonCooldown;
   private int pathRecalcTimer;
   private Vec3 currentPullAnchor;
   private int grappleHoldTicks;

   public SpiderMotherEntity(EntityType<? extends SpiderMotherEntity> type, Level level) {
      super(type, level);
      this.xpReward = 500;
   }

   public static Builder createAttributes() {
      return PathfinderMob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 325.0)
         .add(Attributes.ATTACK_DAMAGE, 8.0)
         .add(Attributes.FOLLOW_RANGE, 50.0)
         .add(Attributes.MOVEMENT_SPEED, 0.28)
         .add(Attributes.ARMOR, 6.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.86)
         .add(Attributes.STEP_HEIGHT, 1.5);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new SpiderMotherEntity.MotherSpiderCombatGoal(this));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 14.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector
         .addGoal(
            2, new NearestAttackableTargetGoal(this, Player.class, true, p -> !(p instanceof Player player && (player.isCreative() || player.isSpectator())))
         );
   }

   protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DATA_ACTION, 0);
      builder.define(DATA_GRAPPLE_HOLDING, false);
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
      super.remove(reason);
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         this.bossBar.setProgress(this.getHealth() / this.getMaxHealth());
         this.tickCooldowns();
         this.tickMinions();
         this.tickAction();
         LivingEntity target = this.getTarget();
         if (target != null && target.isAlive() && !this.isDeadOrDying()) {
            this.faceTarget(target);
         }
      }
   }

   public boolean hurt(DamageSource source, float amount) {
      boolean hurt = super.hurt(source, amount);
      if (hurt && !this.level().isClientSide) {
         if (source.getEntity() instanceof LivingEntity living
            && living.isAlive()
            && !(living instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            this.setTarget(living);
         }

         if (amount >= 12.0F && this.actionTimer <= 0 && this.random.nextFloat() < 0.18F) {
            this.startAction(6, 6);
            this.globalCooldown = Math.max(this.globalCooldown, 5);
         }
      }

      return hurt;
   }

   public int getMotherSpiderAction() {
      return (Integer)this.entityData.get(DATA_ACTION);
   }

   public void setMotherSpiderAction(int action) {
      this.entityData.set(DATA_ACTION, action);
   }

   public boolean isGrappleHolding() {
      return (Boolean)this.entityData.get(DATA_GRAPPLE_HOLDING);
   }

   private void setGrappleHolding(boolean holding) {
      this.entityData.set(DATA_GRAPPLE_HOLDING, holding);
   }

   private void tickCooldowns() {
      if (this.globalCooldown > 0) {
         this.globalCooldown--;
      }

      if (this.biteCooldown > 0) {
         this.biteCooldown--;
      }

      if (this.clawCooldown > 0) {
         this.clawCooldown--;
      }

      if (this.spitCooldown > 0) {
         this.spitCooldown--;
      }

      if (this.grappleCooldown > 0) {
         this.grappleCooldown--;
      }

      if (this.webCooldown > 0) {
         this.webCooldown--;
      }

      if (this.webPullCooldown > 0) {
         this.webPullCooldown--;
      }

      if (this.minionSummonCooldown > 0) {
         this.minionSummonCooldown--;
      }

      if (this.pathRecalcTimer > 0) {
         this.pathRecalcTimer--;
      }
   }

   private void tickAction() {
      if (this.actionTimer > 0) {
         LivingEntity target = this.getTarget();
         int action = this.getMotherSpiderAction();
         int elapsed = this.actionDuration - this.actionTimer;
         if (action == 6 || target != null && target.isAlive()) {
            if (target != null && target.isAlive()) {
               this.getLookControl().setLookAt(target, 35.0F, 35.0F);
            }

            switch (action) {
               case 1:
                  this.tickBite(target, elapsed);
                  break;
               case 2:
                  this.tickClaw(target, elapsed);
                  break;
               case 3:
                  this.tickSpit(target, elapsed);
                  break;
               case 4:
                  this.tickGrapple(target, elapsed);
                  break;
               case 5:
                  this.tickWeb(target, elapsed);
                  break;
               case 6:
                  this.getNavigation().stop();
                  this.stopHorizontalMovement();
                  break;
               case 7:
                  this.tickWebPull(target, elapsed);
            }

            this.actionTimer--;
            if (this.actionTimer <= 0) {
               this.finishAction();
            }
         } else {
            this.cancelAction();
         }
      }
   }

   private void tickBite(LivingEntity target, int elapsed) {
      this.getNavigation().stop();
      this.setSprinting(false);
      if (!this.actionHitApplied && elapsed < 9) {
         this.controlledAttackStep(target, 0.16, 2.0);
      } else {
         this.stopHorizontalMovement();
      }

      if (!this.actionHitApplied && elapsed >= 9 && elapsed <= 20) {
         if (this.hitFocusedTarget(target, 3.35, 0.0F, 0.65)) {
            this.actionHitApplied = true;
         }
      } else if (elapsed > 20) {
         this.actionHitApplied = true;
      }
   }

   private void tickClaw(LivingEntity target, int elapsed) {
      this.getNavigation().stop();
      this.setSprinting(false);
      if (elapsed < 12) {
         this.controlledAttackStep(target, 0.11, 2.5);
      } else {
         this.stopHorizontalMovement();
      }

      if (!this.actionHitApplied && elapsed >= 14 && elapsed <= 22) {
         this.actionHitApplied = true;
         boolean focusedHit = this.hitFocusedTarget(target, 4.45, 0.0F, 0.9);
         this.areaStrike(4.0, 8.0F, 0.75, focusedHit ? target : null);
      } else if (elapsed > 22) {
         this.actionHitApplied = true;
      }
   }

   private void tickSpit(LivingEntity target, int elapsed) {
      this.getNavigation().stop();
      this.setSprinting(false);
      this.stopHorizontalMovement();
      if (!this.actionHitApplied && elapsed >= 18) {
         this.actionHitApplied = true;
         if (target != null && target.isAlive() && this.horizDistSq(target) <= 484.0 && this.hasLineOfSight(target)) {
            this.shootSpitProjectile(target);
         }
      }
   }

   private void tickGrapple(LivingEntity target, int elapsed) {
      this.getNavigation().stop();
      this.setSprinting(true);
      this.faceTarget(target);
      this.setGrappleHolding(this.grappleHoldTicks > 0);
      if (this.grappleHoldTicks > 0) {
         this.setSprinting(false);
         this.stopHorizontalMovement();
         if (this.currentPullAnchor == null) {
            this.currentPullAnchor = this.frontLegAnchor(target);
         }

         this.holdTargetAtAnchor(target, this.currentPullAnchor);
         if (this.grappleHoldTicks % 3 == 0) {
            this.spawnGrappleHoldParticles(target);
         }

         this.grappleHoldTicks--;
         if (this.grappleHoldTicks <= 0) {
            this.setGrappleHolding(false);
         }
      } else {
         if (!this.actionHitApplied && elapsed <= 25) {
            double distSq = this.horizDistSq(target);
            double speed = this.grappleRushSpeed(distSq);
            double stopDistance = distSq > 81.0 ? 2.15 : 1.75;
            this.controlledAttackStep(target, speed, stopDistance);
         } else {
            this.stopHorizontalMovement();
         }

         if (!this.actionHitApplied && elapsed >= 6 && elapsed <= 28) {
            if (this.hitFocusedTarget(target, this.grappleHitRadius(), 0.0F, 0.55)) {
               this.actionHitApplied = true;
               this.currentPullAnchor = this.frontLegAnchor(target);
               this.grappleHoldTicks = 30;
               this.setGrappleHolding(true);
               this.holdTargetAtAnchor(target, this.currentPullAnchor);
               this.stopHorizontalMovement();
            }
         } else if (elapsed > 26) {
            this.actionHitApplied = true;
         }
      }
   }

   private void tickWeb(LivingEntity target, int elapsed) {
      this.getNavigation().stop();
      this.setSprinting(false);
      this.stopHorizontalMovement();
      if (!this.actionHitApplied && elapsed >= 31) {
         this.actionHitApplied = true;
         this.spawnWebPulse(7.2);
         if (this.minionSummonCooldown <= 0) {
            this.spawnWebSpiders(target, 7.2);
            this.minionSummonCooldown = 2400;
         }

         this.areaWeb(7.2, 5.0F);
      }
   }

   private void finishAction() {
      this.setMotherSpiderAction(0);
      this.actionDuration = 0;
      this.actionHitApplied = false;
      this.currentPullAnchor = null;
      this.grappleHoldTicks = 0;
      this.setGrappleHolding(false);
      this.setSprinting(false);
      this.globalCooldown = Math.max(this.globalCooldown, 10);
   }

   private boolean isBusy() {
      return this.actionTimer > 0;
   }

   void cancelAction() {
      if (this.actionTimer > 0) {
         this.setMotherSpiderAction(0);
         this.actionTimer = 0;
         this.actionDuration = 0;
         this.actionHitApplied = false;
         this.currentPullAnchor = null;
         this.grappleHoldTicks = 0;
         this.setGrappleHolding(false);
         this.setSprinting(false);
      }
   }

   void resetCombatEngagement() {
      this.pathRecalcTimer = 0;
   }

   private void tickMinions() {
      String ownerTag = this.minionOwnerTag();
      LivingEntity target = this.getTarget();
      boolean hasCombatTarget = target != null && target.isAlive() && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));

      for (CaveSpider spider : this.level()
         .getEntitiesOfClass(
            CaveSpider.class,
            this.getBoundingBox().inflate(64.0),
            spiderx -> spiderx.isAlive() && spiderx.getTags().contains("twilightforest.spider_mother_minion") && spiderx.getTags().contains(ownerTag)
         )) {
         if (hasCombatTarget) {
            spider.setTarget(target);
         } else {
            spider.setTarget(null);
            if (spider.distanceToSqr(this) <= 10.240000000000002) {
               this.absorbMinion(spider);
            } else {
               spider.getNavigation().moveTo(this.getX(), this.getY(), this.getZ(), 1.15);
            }
         }
      }
   }

   private void tickWebPull(LivingEntity target, int elapsed) {
      this.getNavigation().stop();
      this.setSprinting(false);
      this.stopHorizontalMovement();
      if (target != null && target.isAlive()) {
         if (this.currentPullAnchor == null) {
            this.currentPullAnchor = this.frontLegAnchor(target);
         }

         if (elapsed == 2) {
            this.spawnWebTether(target, 0.0F);
         }

         if (elapsed >= 3 && elapsed <= 30) {
            this.spawnWebTether(target, (elapsed - 3) / 27.0F);
            boolean reachedLegs = this.pullTargetToAnchor(target, this.currentPullAnchor, 1.45, 1.25);
            if (reachedLegs) {
               this.actionHitApplied = true;
            }
         }

         if (elapsed > 30) {
            this.actionHitApplied = true;
         }
      }
   }

   private void startAction(int action, int duration) {
      this.setMotherSpiderAction(action);
      this.actionTimer = duration;
      this.actionDuration = duration;
      this.actionHitApplied = false;
      this.currentPullAnchor = null;
      this.grappleHoldTicks = 0;
      this.setGrappleHolding(false);
      this.getNavigation().stop();
   }

   private void chooseCombatAction(LivingEntity target) {
      if (this.globalCooldown > 0) {
         this.chaseOrHold(target);
      } else {
         double distSq = this.horizDistSq(target);
         double biteRangeSq = this.focusedHitReachSq(target, 3.35);
         double clawRangeSq = this.focusedHitReachSq(target, 4.45);
         double grappleRangeSq = this.focusedHitReachSq(target, 5.15);
         double grappleStartRange = this.grappleStartRange();
         double grappleStartRangeSq = grappleStartRange * grappleStartRange;
         double midRangeSq = 324.0;
         int phase = this.getSpiderPhase();
         if (distSq <= biteRangeSq) {
            if (this.biteCooldown <= 0 && this.random.nextInt(2) == 0) {
               this.startBite();
            } else if (this.clawCooldown <= 0 && this.random.nextInt(3) != 0) {
               this.startClaw();
            } else if (this.grappleCooldown <= 0 && this.random.nextInt(5) == 0) {
               this.startGrapple();
            } else if (this.biteCooldown <= 0) {
               this.startBite();
            } else if (this.clawCooldown <= 0) {
               this.startClaw();
            } else {
               this.chaseTarget(target);
            }
         } else if (distSq <= clawRangeSq) {
            if (this.clawCooldown <= 0 && this.random.nextInt(2) == 0) {
               this.startClaw();
            } else if (this.grappleCooldown <= 0 && this.random.nextInt(5) == 0) {
               this.startGrapple();
            } else {
               this.chaseTarget(target);
            }
         } else if (distSq <= grappleRangeSq) {
            if (this.grappleCooldown <= 0 && this.random.nextInt(4) == 0) {
               this.startGrapple();
            } else {
               this.chaseTarget(target);
            }
         } else if (distSq <= midRangeSq) {
            if (this.webCooldown <= 0 && this.random.nextInt(5) == 0) {
               this.startWeb();
            } else if (this.spitCooldown <= 0 && this.hasLineOfSight(target) && this.random.nextInt(3) != 0) {
               this.startSpit();
            } else if (distSq <= grappleStartRangeSq && this.grappleCooldown <= 0 && this.hasLineOfSight(target) && this.random.nextInt(4) == 0) {
               this.startGrapple();
            } else if (!(distSq > 81.0) && this.random.nextInt(3) == 0) {
               this.chaseTarget(target);
            } else {
               this.chaseTarget(target);
            }
         } else if (this.webPullCooldown <= 0 && this.hasLineOfSight(target)) {
            this.startWebPull();
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
      }
   }

   private void startBite() {
      this.biteCooldown = 30;
      this.startAction(1, 37);
   }

   private void startClaw() {
      this.clawCooldown = 46;
      this.startAction(2, 30);
   }

   private void startSpit() {
      this.spitCooldown = 72;
      this.startAction(3, 40);
   }

   private void startGrapple() {
      this.grappleCooldown = 120;
      this.startAction(4, 64);
   }

   private void startWeb() {
      this.webCooldown = 110;
      this.startAction(5, 70);
   }

   private void startWebPull() {
      this.webPullCooldown = 150;
      this.startAction(7, 44);
   }

   private void chaseTarget(LivingEntity target) {
      double distSq = this.horizDistSq(target);
      int phase = this.getSpiderPhase();
      this.setSprinting(distSq > 64.0 || phase >= 1);
      this.faceTarget(target);
      if (this.hasLineOfSight(target) && distSq > this.attackReachSq(target)) {
         this.getNavigation().stop();

         double directSpeed = switch (phase) {
            case 1 -> 0.36;
            default -> 0.3;
         };
         if (distSq > 196.0) {
            directSpeed += 0.05;
         }

         this.controlledAttackStep(target, directSpeed, 2.25);
         this.pathRecalcTimer = 3;
      } else {
         double speed = switch (phase) {
            case 1 -> 1.08;
            default -> 0.94;
         };
         if (this.pathRecalcTimer <= 0) {
            this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed);
            this.pathRecalcTimer = 5;
         }
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

   private void stopHorizontalMovement() {
      Vec3 movement = this.getDeltaMovement();
      this.setDeltaMovement(0.0, movement.y, 0.0);
   }

   private boolean hitFocusedTarget(LivingEntity target, double radius, float bonusDamage, double knockback) {
      if (target != null && target.isAlive()) {
         if (this.horizDistSq(target) > this.focusedHitReachSq(target, radius)) {
            return false;
         } else if (Math.abs(this.getY() - target.getY()) > 5.0) {
            return false;
         } else {
            float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) + bonusDamage;
            boolean hurt = this.dealBossHit(target, damage);
            if (hurt) {
               target.knockback(knockback, this.getX() - target.getX(), this.getZ() - target.getZ());
               target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
            }

            return hurt;
         }
      } else {
         return false;
      }
   }

   private boolean dealBossHit(LivingEntity target, float damage) {
      target.invulnerableTime = 0;
      return target.hurt(this.level().damageSources().mobAttack(this), damage);
   }

   private void areaStrike(double radius, float damage, double knockback, LivingEntity exclude) {
      for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
         if (victim != exclude && this.isValidVictim(victim) && this.isWithinEffectRadius(victim, radius) && this.dealBossHit(victim, damage)) {
            victim.knockback(knockback, this.getX() - victim.getX(), this.getZ() - victim.getZ());
         }
      }
   }

   private void areaWeb(double radius, float damage) {
      for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius))) {
         if (this.isValidVictim(victim) && this.isWithinEffectRadius(victim, radius) && this.dealBossHit(victim, damage)) {
            this.applyWebDrag(victim, 0.12);
            this.pullTarget(victim, 0.32);
            this.spawnWebHit(victim);
         }
      }
   }

   private void shootSpitProjectile(LivingEntity target) {
      if (!this.level().isClientSide) {
         Vec3 start = this.getEyePosition().add(this.getLookAngle().scale(1.8)).add(0.0, -0.35, 0.0);
         Vec3 aim = target.getEyePosition().add(0.0, -0.2, 0.0);
         SpiderSpitProjectileEntity projectile = new SpiderSpitProjectileEntity(
            (EntityType<? extends SpiderSpitProjectileEntity>)IvarsBosses.SPIDER_SPIT.get(), this.level()
         );
         projectile.setOwner(this);
         projectile.setPos(start.x, start.y, start.z);
         projectile.shootArc(aim, 0.82, 0.045);
         this.level().addFreshEntity(projectile);
         if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPIT, start.x, start.y, start.z, 10, 0.18, 0.12, 0.18, 0.06);
         }
      }
   }

   private void spawnWebPulse(double radius) {
      if (this.level() instanceof ServerLevel serverLevel) {
         double var18 = this.getY() + 0.25;
         byte rings = 3;

         for (int ring = 1; ring <= rings; ring++) {
            double ringRadius = radius * ring / rings;
            int count = Math.max(18, (int)(ringRadius * 10.0));

            for (int i = 0; i < count; i++) {
               double angle = (Math.PI * 2) * i / count;
               double x = this.getX() + Math.cos(angle) * ringRadius;
               double z = this.getZ() + Math.sin(angle) * ringRadius;
               serverLevel.sendParticles(ParticleTypes.CLOUD, x, var18 + ring * 0.05, z, 1, 0.03, 0.02, 0.03, 0.0);
               if (i % 4 == 0) {
                  serverLevel.sendParticles(ParticleTypes.WITCH, x, var18 + 0.12, z, 1, 0.02, 0.02, 0.02, 0.0);
               }
            }
         }

         serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 1.0, this.getZ(), 28, 1.2, 0.45, 1.2, 0.04);
      }
   }

   private void spawnWebSpiders(LivingEntity target, double radius) {
      if (this.level() instanceof ServerLevel serverLevel) {
         double var18 = this.random.nextDouble() * (float) (Math.PI * 2);

         for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = radius * ring / 3.0;
            double angle = var18 + (ring - 1) * (float) (Math.PI * 2) / 3.0;
            double spawnX = this.getX() + Math.cos(angle) * ringRadius;
            double spawnZ = this.getZ() + Math.sin(angle) * ringRadius;
            BlockPos surface = serverLevel.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(spawnX, this.getY(), spawnZ));
            CaveSpider spider = (CaveSpider)EntityType.CAVE_SPIDER.create(serverLevel);
            if (spider != null) {
               spider.moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5, serverLevel.random.nextFloat() * 360.0F, 0.0F);
               if (serverLevel.noCollision(spider)) {
                  spider.setPersistenceRequired();
                  spider.addTag("twilightforest.spider_mother_minion");
                  spider.addTag(this.minionOwnerTag());
                  if (target != null && target.isAlive()) {
                     spider.setTarget(target);
                  }

                  serverLevel.addFreshEntity(spider);
                  serverLevel.sendParticles(ParticleTypes.CLOUD, spider.getX(), spider.getY() + 0.2, spider.getZ(), 10, 0.18, 0.08, 0.18, 0.03);
                  serverLevel.sendParticles(ParticleTypes.WITCH, spider.getX(), spider.getY() + 0.3, spider.getZ(), 4, 0.12, 0.06, 0.12, 0.01);
               }
            }
         }
      }
   }

   private void absorbMinion(CaveSpider spider) {
      if (this.getHealth() < this.getMaxHealth()) {
         this.heal(8.0F);
      }

      if (this.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(
            ParticleTypes.HEART,
            this.getX(),
            this.getY() + this.getBbHeight() * 0.65,
            this.getZ(),
            4,
            this.getBbWidth() * 0.25,
            this.getBbHeight() * 0.18,
            this.getBbWidth() * 0.25,
            0.02
         );
         serverLevel.sendParticles(ParticleTypes.POOF, spider.getX(), spider.getY() + 0.2, spider.getZ(), 8, 0.18, 0.08, 0.18, 0.02);
      }

      spider.discard();
   }

   private String minionOwnerTag() {
      return "twilightforest.spider_mother_owner_" + this.getUUID();
   }

   private void spawnWebHit(LivingEntity target) {
      if (this.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(
            ParticleTypes.CLOUD,
            target.getX(),
            target.getY() + target.getBbHeight() * 0.55,
            target.getZ(),
            18,
            target.getBbWidth() * 0.45,
            target.getBbHeight() * 0.25,
            target.getBbWidth() * 0.45,
            0.02
         );
         serverLevel.sendParticles(
            ParticleTypes.WITCH,
            target.getX(),
            target.getY() + target.getBbHeight() * 0.65,
            target.getZ(),
            8,
            target.getBbWidth() * 0.35,
            target.getBbHeight() * 0.2,
            target.getBbWidth() * 0.35,
            0.01
         );
      }
   }

   private void spawnWebTether(LivingEntity target, float progress) {
      if (this.level() instanceof ServerLevel serverLevel) {
         if (target != null && target.isAlive()) {
            Vec3 start = this.getEyePosition().add(this.getLookAngle().scale(1.65)).add(0.0, -0.28, 0.0);
            Vec3 end = target.getEyePosition().add(0.0, -0.15, 0.0);
            Vec3 delta = end.subtract(start);
            double distance = delta.length();
            if (!(distance < 0.1)) {
               Vec3 step = delta.normalize().scale(0.85);
               int points = Math.max(4, (int)(distance / 0.85));

               for (int i = 0; i <= points; i++) {
                  Vec3 point = start.add(step.scale(i));
                  if (i % 2 == 0) {
                     serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
                  }

                  if (i % 4 == 0) {
                     serverLevel.sendParticles(ParticleTypes.CLOUD, point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.0);
                  }
               }

               if (progress > 0.15F) {
                  serverLevel.sendParticles(ParticleTypes.POOF, end.x, end.y, end.z, 3, 0.1, 0.1, 0.1, 0.01);
               }
            }
         }
      }
   }

   private void applyWebDrag(LivingEntity target, double horizontalScale) {
      Vec3 movement = target.getDeltaMovement();
      target.setDeltaMovement(movement.x * horizontalScale, Math.min(movement.y, 0.05), movement.z * horizontalScale);
   }

   private void pullTarget(LivingEntity target, double strength) {
      if (target != null && target.isAlive()) {
         Vec3 delta = this.position().subtract(target.position());
         Vec3 flat = new Vec3(delta.x, 0.0, delta.z);
         if (!(flat.lengthSqr() < 1.0E-4)) {
            Vec3 pull = flat.normalize().scale(strength);
            Vec3 current = target.getDeltaMovement();
            target.setDeltaMovement(current.x * 0.25 + pull.x, Math.max(current.y * 0.35, 0.12), current.z * 0.25 + pull.z);
            if (target instanceof ServerPlayer serverPlayer) {
               serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }

            target.fallDistance = 0.0F;
         }
      }
   }

   private Vec3 frontLegAnchor(LivingEntity target) {
      Vec3 fromSpiderToTarget = target.position().subtract(this.position());
      Vec3 flat = new Vec3(fromSpiderToTarget.x, 0.0, fromSpiderToTarget.z);
      if (flat.lengthSqr() < 1.0E-4) {
         Vec3 look = this.getLookAngle();
         flat = new Vec3(look.x, 0.0, look.z);
      }

      if (flat.lengthSqr() < 1.0E-4) {
         flat = new Vec3(0.0, 0.0, 1.0);
      }

      Vec3 direction = flat.normalize();
      double frontOffset = this.getBbWidth() * 0.5 + target.getBbWidth() * 0.35 + 0.45;
      return this.position().add(direction.scale(frontOffset)).add(0.0, 0.08, 0.0);
   }

   private boolean pullTargetToAnchor(LivingEntity target, Vec3 anchor, double strength, double stopDistance) {
      if (target != null && target.isAlive()) {
         Vec3 delta = anchor.subtract(target.position());
         Vec3 flat = new Vec3(delta.x, 0.0, delta.z);
         double distance = flat.length();
         Vec3 current = target.getDeltaMovement();
         if (!(distance <= stopDistance) && !(distance < 1.0E-4)) {
            double scaledStrength = Mth.clamp((distance - stopDistance) * 0.28, 0.16, strength);
            Vec3 pull = flat.normalize().scale(scaledStrength);
            target.setDeltaMovement(current.x * 0.16 + pull.x, Math.max(current.y * 0.25, 0.08), current.z * 0.16 + pull.z);
            this.syncTargetMotion(target);
            target.fallDistance = 0.0F;
            return false;
         } else {
            target.setDeltaMovement(current.x * 0.08, Math.min(current.y * 0.25, 0.04), current.z * 0.08);
            this.syncTargetMotion(target);
            target.fallDistance = 0.0F;
            return true;
         }
      } else {
         return false;
      }
   }

   private void holdTargetAtAnchor(LivingEntity target, Vec3 anchor) {
      this.pullTargetToAnchor(target, anchor, 1.2, 0.45);
      Vec3 delta = anchor.subtract(target.position());
      Vec3 flat = new Vec3(delta.x, 0.0, delta.z);
      if (flat.lengthSqr() <= 0.2025) {
         Vec3 movement = target.getDeltaMovement();
         target.setDeltaMovement(movement.x * 0.03, Math.min(movement.y, 0.02), movement.z * 0.03);
         this.syncTargetMotion(target);
      }

      target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 5, false, false));
      target.fallDistance = 0.0F;
   }

   private void spawnGrappleHoldParticles(LivingEntity target) {
      if (this.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(
            ParticleTypes.CLOUD,
            target.getX(),
            target.getY() + target.getBbHeight() * 0.45,
            target.getZ(),
            4,
            target.getBbWidth() * 0.35,
            target.getBbHeight() * 0.2,
            target.getBbWidth() * 0.35,
            0.01
         );
         serverLevel.sendParticles(
            ParticleTypes.END_ROD,
            target.getX(),
            target.getY() + target.getBbHeight() * 0.55,
            target.getZ(),
            2,
            target.getBbWidth() * 0.25,
            target.getBbHeight() * 0.15,
            target.getBbWidth() * 0.25,
            0.0
         );
      }
   }

   private void syncTargetMotion(LivingEntity target) {
      if (target instanceof ServerPlayer serverPlayer) {
         serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
      }
   }

   private boolean isValidVictim(LivingEntity victim) {
      if (victim != this && victim.isAlive() && !victim.isAlliedTo(this)) {
         if (!(victim instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            LivingEntity currentTarget = this.getTarget();
            return currentTarget != null && currentTarget.isAlive() && (victim == currentTarget || victim instanceof Player);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isWithinEffectRadius(LivingEntity victim, double radius) {
      return victim.distanceToSqr(this.getX(), this.getY(), this.getZ()) <= radius * radius;
   }

   private double grappleStartRange() {
      return switch (this.getSpiderPhase()) {
         case 1 -> 13.5;
         default -> 11.0;
      };
   }

   private double grappleRushSpeed(double distSq) {
      if (distSq > 144.0) {
         return 0.78;
      } else {
         return distSq > 49.0 ? 0.62 : 0.46;
      }
   }

   private double grappleHitRadius() {
      return switch (this.getSpiderPhase()) {
         case 1 -> 5.0;
         default -> 4.7;
      };
   }

   private double attackReachSq(LivingEntity target) {
      double reach = this.getBbWidth() * 0.5 + target.getBbWidth() + 0.8;
      return reach * reach;
   }

   private double focusedHitReachSq(LivingEntity target, double radius) {
      double bodyAwareRadius = this.getBbWidth() * 0.5 + target.getBbWidth() + 0.45;
      double reach = Math.max(radius, bodyAwareRadius);
      return reach * reach;
   }

   private double horizDistSq(LivingEntity target) {
      double dx = this.getX() - target.getX();
      double dz = this.getZ() - target.getZ();
      return dx * dx + dz * dz;
   }

   private int getSpiderPhase() {
      float healthPct = this.getMaxHealth() <= 0.0F ? 1.0F : this.getHealth() / this.getMaxHealth();
      return healthPct < 0.5F ? 1 : 0;
   }

   private RawAnimation idleAnimation() {
      return switch (this.getSpiderPhase()) {
         case 1 -> ANIM_IDLE_MISSING_1;
         default -> ANIM_IDLE;
      };
   }

   private RawAnimation walkAnimation() {
      return switch (this.getSpiderPhase()) {
         case 1 -> ANIM_WALK_MISSING_1;
         default -> ANIM_WALK;
      };
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SPIDER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource damageSource) {
      return SoundEvents.SPIDER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SPIDER_DEATH;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController(this, "mother_spider_controller", 2, this::animPredicate));
   }

   private <T extends GeoAnimatable> PlayState animPredicate(AnimationState<T> state) {
      if (this.isDeadOrDying()) {
         return state.setAndContinue(ANIM_DEATH);
      } else {
         return switch (this.getMotherSpiderAction()) {
            case 1 -> state.setAndContinue(ANIM_BITE);
            case 2 -> state.setAndContinue(ANIM_CLAW);
            case 3 -> state.setAndContinue(ANIM_SPIT);
            case 4 -> state.setAndContinue(this.isGrappleHolding() ? ANIM_GRAPPLE_HOLD : ANIM_GRAPPLE);
            case 5 -> state.setAndContinue(ANIM_WEB);
            case 6 -> state.setAndContinue(ANIM_STUN_HIT);
            case 7 -> state.setAndContinue(ANIM_SPIT);
            default -> this.getDeltaMovement().horizontalDistance() > 0.025
               ? state.setAndContinue(this.walkAnimation())
               : state.setAndContinue(this.idleAnimation());
         };
      }
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.animCache;
   }

   private static final class MotherSpiderCombatGoal extends Goal {
      private final SpiderMotherEntity boss;

      MotherSpiderCombatGoal(SpiderMotherEntity boss) {
         this.boss = boss;
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
      }

      public boolean canUse() {
         LivingEntity target = this.boss.getTarget();
         return target != null && target.isAlive() && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
      }

      public boolean canContinueToUse() {
         LivingEntity target = this.boss.getTarget();
         return target != null && target.isAlive() && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
      }

      public void stop() {
         this.boss.setSprinting(false);
         this.boss.getNavigation().stop();
         this.boss.cancelAction();
         this.boss.resetCombatEngagement();
      }

      public void tick() {
         LivingEntity target = this.boss.getTarget();
         if (target != null && target.isAlive()) {
            this.boss.getLookControl().setLookAt(target, 25.0F, 25.0F);
            if (!this.boss.isBusy()) {
               this.boss.chooseCombatAction(target);
            }
         }
      }
   }
}
