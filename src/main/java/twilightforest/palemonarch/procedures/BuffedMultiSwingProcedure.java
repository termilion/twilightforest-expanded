package twilightforest.palemonarch.procedures;

import java.util.Comparator;
import twilightforest.palemonarch.PaleMonarchMod;
import twilightforest.palemonarch.entity.PaleMonarchEntity;
import twilightforest.palemonarch.init.PaleMonarchModMobEffects;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class BuffedMultiSwingProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof PaleMonarchEntity _datEntSetL) {
            _datEntSetL.getEntityData().set(PaleMonarchEntity.DATA_IsBuffed, false);
         }

         if (entity instanceof PaleMonarchEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_attackcooldown, 230);
         }

         if (entity instanceof PaleMonarchEntity) {
            ((PaleMonarchEntity)entity).setAnimation("SwingCombo");
         }

         PaleMonarchMod.queueServerWork(
            5,
            () -> {
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

               entity.setDeltaMovement(new Vec3(entity.getLookAngle().x, 0.0, entity.getLookAngle().z));
            }
         );
         PaleMonarchMod.queueServerWork(
            10,
            () -> {
               if (entity.isAlive()) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null
                        && entityiterator == (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
                        entityiterator.invulnerableTime = 0;
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 14.0F);
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
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            20,
            () -> {
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

               entity.setDeltaMovement(new Vec3(entity.getLookAngle().x, 0.0, entity.getLookAngle().z));
            }
         );
         PaleMonarchMod.queueServerWork(
            25,
            () -> {
               if (entity.isAlive()) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null
                        && entityiterator == (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
                        entityiterator.invulnerableTime = 0;
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 16.0F);
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
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            40,
            () -> {
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

               entity.setDeltaMovement(new Vec3(entity.getLookAngle().x, 0.0, entity.getLookAngle().z));
            }
         );
         PaleMonarchMod.queueServerWork(45, () -> {});
         PaleMonarchMod.queueServerWork(
            60,
            () -> {
               if (entity instanceof PaleMonarchEntity) {
                  ((PaleMonarchEntity)entity).setAnimation("empty");
               }

               if (entity instanceof PaleMonarchEntity) {
                  ((PaleMonarchEntity)entity).setAnimation("SwingComboDouble");
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
            }
         );
         PaleMonarchMod.queueServerWork(
            65,
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

               entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 0.5, 0.0, entity.getLookAngle().z * 0.5));
            }
         );
         PaleMonarchMod.queueServerWork(
            70,
            () -> {
               if (entity.isAlive()) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null
                        && entityiterator == (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
                        entityiterator.invulnerableTime = 0;
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 16.0F);
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
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            75,
            () -> {
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
            }
         );
         PaleMonarchMod.queueServerWork(
            80,
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

               entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * 0.5, 0.0, entity.getLookAngle().z * 0.5));
            }
         );
         PaleMonarchMod.queueServerWork(
            85,
            () -> {
               if (entity.isAlive()) {
                  Vec3 _center = new Vec3(entity.getX() + entity.getLookAngle().x, entity.getY(), entity.getZ() + entity.getLookAngle().z);

                  for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.5), e -> true)
                     .stream()
                     .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                     .toList()) {
                     if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null
                        && entityiterator == (entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null)) {
                        entityiterator.invulnerableTime = 0;
                        entityiterator.hurt(new DamageSource(world.holderOrThrow(DamageTypes.MOB_ATTACK), entity), 16.0F);
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
                     }
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(95, () -> JumpafterpunchProcedure.execute(world, x, y, z, entity));
         PaleMonarchMod.queueServerWork(
            155,
            () -> {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        null,
                        BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F,
                        false
                     );
                  }
               }

               if (entity instanceof PaleMonarchEntity) {
                  ((PaleMonarchEntity)entity).setAnimation("empty");
               }

               if (entity instanceof PaleMonarchEntity) {
                  ((PaleMonarchEntity)entity).setAnimation("Tired");
               }

               if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide()) {
                  _entity.addEffect(new MobEffectInstance(PaleMonarchModMobEffects.TIRED, 60, 2));
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            165,
            () -> {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        null,
                        BlockPos.containing(entity.getX(), entity.getY(), entity.getZ()),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.stone.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        0.0F
                     );
                  } else {
                     _level.playLocalSound(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.stone.break")),
                        SoundSource.NEUTRAL,
                        1.0F,
                        0.0F,
                        false
                     );
                  }
               }
            }
         );
      }
   }
}
