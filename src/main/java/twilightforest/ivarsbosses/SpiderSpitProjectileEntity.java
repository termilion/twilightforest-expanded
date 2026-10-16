package twilightforest.ivarsbosses;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled from Ivar's Bosses
public class SpiderSpitProjectileEntity extends Entity {
   private static final double HITBOX_INFLATE = 0.45;
   private static final float DAMAGE = 8.0F;
   private static final int POISON_TICKS = 60;
   private static final int MAX_LIFETIME_TICKS = 80;
   private static final double DEFAULT_GRAVITY = 0.045;
   private LivingEntity owner;
   private double gravity = 0.045;
   private float spin;

   public SpiderSpitProjectileEntity(EntityType<? extends SpiderSpitProjectileEntity> type, Level level) {
      super(type, level);
      this.noPhysics = true;
      this.setNoGravity(true);
   }

   public void setOwner(LivingEntity owner) {
      this.owner = owner;
   }

   public void shootArc(Vec3 targetPos, double speed, double gravity) {
      Vec3 delta = targetPos.subtract(this.position());
      if (!(delta.lengthSqr() < 1.0E-4)) {
         this.gravity = gravity;
         this.setNoGravity(true);
         double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
         double flightTime = Mth.clamp(horizontalDistance / Math.max(speed, 0.05), 12.0, 30.0);
         double vx = delta.x / flightTime;
         double vz = delta.z / flightTime;
         double vy = (delta.y + 0.5 * gravity * flightTime * flightTime) / flightTime;
         this.setDeltaMovement(vx, vy, vz);
         this.updateRotationFromVelocity();
      }
   }

   protected void defineSynchedData(Builder builder) {
   }

   public void tick() {
      super.tick();
      if (this.tickCount > 80) {
         this.discard();
      } else {
         Vec3 movement = this.getDeltaMovement();
         Vec3 start = this.position();
         Vec3 end = start.add(movement);
         this.spawnFlightParticles();
         if (movement.lengthSqr() > 1.0E-4) {
            this.updateRotationFromVelocity();
            this.spin = (this.spin + (float)(movement.horizontalDistance() * 420.0 + 24.0)) % 360.0F;
         }

         if (!this.level().isClientSide) {
            HitResult blockHit = this.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
            Vec3 collisionEnd = blockHit.getType() == Type.MISS ? end : blockHit.getLocation();
            LivingEntity entityHit = this.findEntityHit(start, collisionEnd);
            if (entityHit != null) {
               this.setPos(entityHit.getX(), entityHit.getY() + entityHit.getBbHeight() * 0.5, entityHit.getZ());
               this.hitEntity(entityHit);
               return;
            }

            if (blockHit.getType() != Type.MISS) {
               this.setPos(collisionEnd.x, collisionEnd.y, collisionEnd.z);
               this.hitBlock((BlockHitResult)blockHit);
               return;
            }
         }

         this.setPos(end.x, end.y, end.z);
         this.setDeltaMovement(movement.x, movement.y - this.gravity, movement.z);
      }
   }

   private LivingEntity findEntityHit(Vec3 start, Vec3 end) {
      AABB searchBox = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(0.45);
      List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, this::canHit);
      LivingEntity nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (LivingEntity candidate : candidates) {
         AABB targetBox = candidate.getBoundingBox().inflate(0.45);
         boolean intersects = targetBox.contains(start) || targetBox.contains(end) || targetBox.clip(start, end).isPresent();
         if (intersects) {
            double distance = candidate.distanceToSqr(start);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = candidate;
            }
         }
      }

      return nearest;
   }

   private boolean canHit(LivingEntity entity) {
      if (!entity.isAlive() || entity == this.owner) {
         return false;
      } else if (this.tickCount < 4 && this.owner != null && entity.is(this.owner)) {
         return false;
      } else {
         return this.owner != null && entity.isAlliedTo(this.owner)
            ? false
            : !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
      }
   }

   public boolean isPickable() {
      return true;
   }

   public boolean isAttackable() {
      return true;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (!this.level().isClientSide && !this.isRemoved()) {
         Entity attacker = source.getEntity();
         if (attacker != null && this.owner != null && attacker.is(this.owner)) {
            return false;
         } else {
            this.spawnImpactParticles(this.position());
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.8F, 1.25F);
            this.discard();
            return true;
         }
      } else {
         return true;
      }
   }

   private void hitEntity(LivingEntity target) {
      if (!this.level().isClientSide) {
         target.invulnerableTime = 0;
         DamageSource damageSource = this.owner != null ? this.level().damageSources().mobAttack(this.owner) : this.level().damageSources().generic();
         if (target.hurt(damageSource, 8.0F)) {
            Vec3 movement = target.getDeltaMovement();
            target.setDeltaMovement(movement.x * 0.16, Math.min(movement.y, 0.05), movement.z * 0.16);
         }

         target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
         this.spawnImpactParticles(target.position().add(0.0, target.getBbHeight() * 0.55, 0.0));
         this.discard();
      }
   }

   private void hitBlock(BlockHitResult hit) {
      if (!this.level().isClientSide) {
         this.spawnImpactParticles(hit.getLocation());
         this.discard();
      }
   }

   private void spawnFlightParticles() {
      if (this.level().isClientSide) {
         this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
         this.level().addParticle(ParticleTypes.SPIT, this.getX(), this.getY() + 0.05, this.getZ(), 0.0, 0.0, 0.0);
      }
   }

   private void spawnImpactParticles(Vec3 position) {
      if (this.level() instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.CLOUD, position.x, position.y, position.z, 24, 0.35, 0.25, 0.35, 0.04);
         serverLevel.sendParticles(ParticleTypes.WITCH, position.x, position.y + 0.05, position.z, 10, 0.22, 0.18, 0.22, 0.02);
      }
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   private void updateRotationFromVelocity() {
      Vec3 movement = this.getDeltaMovement();
      if (!(movement.lengthSqr() < 1.0E-4)) {
         float yaw = (float)(Math.atan2(movement.z, movement.x) * (180.0 / Math.PI)) - 90.0F;
         float pitch = (float)(-Math.atan2(movement.y, movement.horizontalDistance()) * (180.0 / Math.PI));
         this.setYRot(yaw);
         this.setXRot(pitch);
      }
   }

   public float getSpin(float partialTick) {
      return this.spin + partialTick * 24.0F;
   }
}
