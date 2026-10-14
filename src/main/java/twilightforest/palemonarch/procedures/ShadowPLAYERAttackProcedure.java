package twilightforest.palemonarch.procedures;

import java.util.Comparator;
import twilightforest.palemonarch.PaleMonarchMod;
import twilightforest.palemonarch.entity.ShadowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class ShadowPLAYERAttackProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double amountofstuff = 0.0;
         if (entity instanceof ShadowEntity) {
            ((ShadowEntity)entity).setAnimation("lionclawlol");
         }

         entity.getPersistentData().putDouble("shadowhit", 0.0);
         PaleMonarchMod.queueServerWork(10, () -> entity.push(entity.getLookAngle().x * 0.5, 0.0, entity.getLookAngle().z * 0.5));
         PaleMonarchMod.queueServerWork(
            20,
            () -> {
               entity.push(entity.getLookAngle().x * 0.5, 0.0, entity.getLookAngle().z * 0.5);
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F,
                        false
                     );
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            25,
            () -> {
               entity.push(entity.getLookAngle().x * 0.5, 0.0, entity.getLookAngle().z * 0.5);
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F,
                        false
                     );
                  }
               }

               Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator != entity) {
                     entity.getPersistentData().putDouble("shadowhit", 1.0);
                     entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 14.0F);
                  }
               }

               if (world instanceof Level _levelx) {
                  if (!_levelx.isClientSide()) {
                     _levelx.playSound(
                        null,
                        BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.anvil.land")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F
                     );
                  } else {
                     _levelx.playLocalSound(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.anvil.land")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        1.0F,
                        false
                     );
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            26,
            () -> {
               entity.push(entity.getLookAngle().x * 0.1, 0.0, entity.getLookAngle().z * 0.1);
               if (entity.getPersistentData().getDouble("shadowhit") == 100.0) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity) {
                        entity.getPersistentData().putDouble("shadowhit", 1.0);
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 14.0F);
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            27,
            () -> {
               entity.push(entity.getLookAngle().x * 0.1, 0.0, entity.getLookAngle().z * 0.1);
               if (entity.getPersistentData().getDouble("shadowhit") == 100.0) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity) {
                        entity.getPersistentData().putDouble("shadowhit", 1.0);
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 14.0F);
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            28,
            () -> {
               entity.push(entity.getLookAngle().x * 0.1, 0.0, entity.getLookAngle().z * 0.1);
               if (entity.getPersistentData().getDouble("shadowhit") == 100.0) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity) {
                        entity.getPersistentData().putDouble("shadowhit", 1.0);
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 14.0F);
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            29,
            () -> {
               entity.push(entity.getLookAngle().x * 0.1, 0.0, entity.getLookAngle().z * 0.1);
               if (entity.getPersistentData().getDouble("shadowhit") == 100.0) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity) {
                        entity.getPersistentData().putDouble("shadowhit", 1.0);
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 14.0F);
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            30,
            () -> {
               entity.push(entity.getLookAngle().x * 0.1, 0.0, entity.getLookAngle().z * 0.1);
               if (entity.getPersistentData().getDouble("shadowhit") == 100.0) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.0), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if (entityiterator != entity) {
                        entity.getPersistentData().putDouble("shadowhit", 1.0);
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 14.0F);
                     }
                  }
               }
            }
         );
      }
   }
}
