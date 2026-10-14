package twilightforest.palemonarch.procedures;

import java.util.Comparator;
import twilightforest.palemonarch.PaleMonarchMod;
import twilightforest.palemonarch.entity.FalseKing1Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class Shadow1AttackProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double amountofstuff = 0.0;
         if (entity instanceof FalseKing1Entity) {
            ((FalseKing1Entity)entity).setAnimation("lionclawlol");
         }

         PaleMonarchMod.queueServerWork(
            10,
            () -> {
               if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
                  if (entity instanceof FalseKing1Entity _datEntSetL) {
                     _datEntSetL.getEntityData().set(FalseKing1Entity.DATA_didteleport, true);
                  }

                  if (entity instanceof LivingEntity _entity && _entity.hasLineOfSight(entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null)) {
                     entity.teleportTo(
                        (entity instanceof Mob _mobEntxxxx ? _mobEntxxxx.getTarget() : null).getX(),
                        (entity instanceof Mob _mobEntxxx ? _mobEntxxx.getTarget() : null).getY(),
                        (entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null).getZ()
                     );
                     if (entity instanceof ServerPlayer _serverPlayer) {
                        _serverPlayer.connection
                           .teleport(
                              (entity instanceof Mob _mobEntxxxxxxx ? _mobEntxxxxxxx.getTarget() : null).getX(),
                              (entity instanceof Mob _mobEntxxxxxx ? _mobEntxxxxxx.getTarget() : null).getY(),
                              (entity instanceof Mob _mobEntxxxxx ? _mobEntxxxxx.getTarget() : null).getZ(),
                              entity.getYRot(),
                              entity.getXRot()
                           );
                     }
                  } else {
                     entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 1.5, 0.0, entity.getLookAngle().z * 1.5));
                  }
               } else {
                  if (entity instanceof FalseKing1Entity _datEntSetL) {
                     _datEntSetL.getEntityData().set(FalseKing1Entity.DATA_didteleport, true);
                  }

                  entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 1.5, 0.0, entity.getLookAngle().z * 1.5));
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            15,
            () -> {
               if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null
                  && entity instanceof FalseKing1Entity _datEntL23
                  && (Boolean)_datEntL23.getEntityData().get(FalseKing1Entity.DATA_didteleport)) {
                  if (entity instanceof FalseKing1Entity _datEntSetL) {
                     _datEntSetL.getEntityData().set(FalseKing1Entity.DATA_didteleport, true);
                  }

                  if (entity instanceof LivingEntity _entity && _entity.hasLineOfSight(entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null)) {
                     entity.teleportTo(
                        (entity instanceof Mob _mobEntxxxx ? _mobEntxxxx.getTarget() : null).getX(),
                        (entity instanceof Mob _mobEntxxx ? _mobEntxxx.getTarget() : null).getY(),
                        (entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null).getZ()
                     );
                     if (entity instanceof ServerPlayer _serverPlayer) {
                        _serverPlayer.connection
                           .teleport(
                              (entity instanceof Mob _mobEntxxxxxxx ? _mobEntxxxxxxx.getTarget() : null).getX(),
                              (entity instanceof Mob _mobEntxxxxxx ? _mobEntxxxxxx.getTarget() : null).getY(),
                              (entity instanceof Mob _mobEntxxxxx ? _mobEntxxxxx.getTarget() : null).getZ(),
                              entity.getYRot(),
                              entity.getXRot()
                           );
                     }
                  } else {
                     entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 1.5, 0.0, entity.getLookAngle().z * 1.5));
                  }
               } else {
                  if (entity instanceof FalseKing1Entity _datEntSetL) {
                     _datEntSetL.getEntityData().set(FalseKing1Entity.DATA_didteleport, true);
                  }

                  entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 1.5, 0.0, entity.getLookAngle().z * 1.5));
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            20,
            () -> {
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
                  if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null
                     && entityiterator == (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
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

               if (world instanceof Level _levelxx) {
                  if (!_levelxx.isClientSide()) {
                     _levelxx.playSound(
                        null,
                        BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.anvil.land")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F
                     );
                  } else {
                     _levelxx.playLocalSound(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.anvil.land")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F,
                        false
                     );
                  }
               }
            }
         );
      }
   }
}
