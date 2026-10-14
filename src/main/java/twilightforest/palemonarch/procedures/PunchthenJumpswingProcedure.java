package twilightforest.palemonarch.procedures;

import java.util.Comparator;
import twilightforest.palemonarch.PaleMonarchMod;
import twilightforest.palemonarch.entity.PaleMonarchEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class PunchthenJumpswingProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         double amountofstuff = 0.0;
         amountofstuff = 0.0;
         if (entity instanceof PaleMonarchEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_attackcooldown, 50);
         }

         if (entity instanceof PaleMonarchEntity) {
            ((PaleMonarchEntity)entity).setAnimation("empty");
         }

         if (entity instanceof PaleMonarchEntity) {
            ((PaleMonarchEntity)entity).setAnimation("Punch");
         }

         PaleMonarchMod.queueServerWork(
            10,
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

               if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
                  entity.lookAt(
                     Anchor.EYES,
                     new Vec3(
                        (entity instanceof Mob _mobEntxxx ? _mobEntxxx.getTarget() : null).getX(),
                        (entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null).getY(),
                        (entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null).getZ()
                     )
                  );
               }

               entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 0.5, 0.0, entity.getLookAngle().z * 0.5));
            }
         );
         PaleMonarchMod.queueServerWork(
            15,
            () -> {
               Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null
                     && entityiterator == (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
                     entityiterator.hurt(
                        new DamageSource(
                           world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("twilightforest:armor_pierce"))), entity
                        ),
                        4.0F
                     );
                     if (world instanceof Level _level) {
                        if (!_level.isClientSide()) {
                           _level.playSound(
                              null,
                              BlockPos.containing(entityiterator.getX(), entityiterator.getY(), entityiterator.getZ()),
                              (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.anvil.land")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              1.0F
                           );
                        } else {
                           _level.playLocalSound(
                              entityiterator.getX(),
                              entityiterator.getY(),
                              entityiterator.getZ(),
                              (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.anvil.land")),
                              SoundSource.NEUTRAL,
                              1.0F,
                              1.0F,
                              false
                           );
                        }
                     }

                     entityiterator.setDeltaMovement(new Vec3(entity.getLookAngle().x * 2.0, 0.0, entity.getLookAngle().z * 2.0));
                     if (entity instanceof LivingEntity _entity && _entity.hasLineOfSight(entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null)) {
                        if (entity instanceof PaleMonarchEntity _datEntSetIxx) {
                           _datEntSetIxx.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 60);
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetIx) {
                           _datEntSetIx.getEntityData().set(PaleMonarchEntity.DATA_attackcooldown, 60);
                        }

                        JumpafterpunchProcedure.execute(world, x, y, z, entity);
                     }
                  }
               }
            }
         );
      }
   }
}
