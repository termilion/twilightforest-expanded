package twilightforest.palemonarch.procedures;

import twilightforest.palemonarch.PaleMonarchMod;
import twilightforest.palemonarch.entity.PaleMonarchEntity;
import twilightforest.palemonarch.init.PaleMonarchModEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class SummonFalseKing1Procedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof PaleMonarchEntity _datEntSetI) {
            _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_attackcooldown, 25);
         }

         if (entity instanceof PaleMonarchEntity) {
            ((PaleMonarchEntity)entity).setAnimation("empty");
         }

         if (entity instanceof PaleMonarchEntity) {
            ((PaleMonarchEntity)entity).setAnimation("NewCastingMagic");
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

         PaleMonarchMod.queueServerWork(
            15,
            () -> {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        2.0F,
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
               if ((entity instanceof Mob _mobEntxxxx ? _mobEntxxxx.getTarget() : null) != null) {
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
                        2.0F,
                        1.0F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.player.attack.nodamage")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F,
                        false
                     );
                  }
               }
            }
         );
         PaleMonarchMod.queueServerWork(
            30,
            () -> {
               if (world instanceof Level _level) {
                  if (!_level.isClientSide()) {
                     _level.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.explode")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        0.75F
                     );
                  } else {
                     _level.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.explode")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        0.75F,
                        false
                     );
                  }
               }

               if (world instanceof Level _levelx) {
                  if (!_levelx.isClientSide()) {
                     _levelx.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.candle.extinguish")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F
                     );
                  } else {
                     _levelx.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.candle.extinguish")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F,
                        false
                     );
                  }
               }

               if (world instanceof Level _levelxx) {
                  if (!_levelxx.isClientSide()) {
                     _levelxx.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.amethyst_block.place")),
                        SoundSource.NEUTRAL,
                        0.25F,
                        0.7F
                     );
                  } else {
                     _levelxx.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.amethyst_block.place")),
                        SoundSource.NEUTRAL,
                        0.25F,
                        0.7F,
                        false
                     );
                  }
               }

               if (world instanceof ServerLevel _serverLevel) {
                  Entity entityinstance = ((EntityType)PaleMonarchModEntities.FALSE_KING_1.get())
                     .create(
                        _serverLevel,
                        null,
                        BlockPos.containing(
                           entity.getX() + Math.cos((entity.getYRot() + 90.0F) * 0.017453277777777776),
                           entity.getY(),
                           entity.getZ() + Math.sin((entity.getYRot() + 90.0F) * 0.017453277777777776)
                        ),
                        MobSpawnType.MOB_SUMMONED,
                        false,
                        false
                     );
                  if (entityinstance != null) {
                     entityinstance.setYRot(world.getRandom().nextFloat() * 360.0F);
                     if (world instanceof ServerLevel _levelxxx) {
                        _levelxxx.sendParticles(
                           ParticleTypes.LARGE_SMOKE, entityinstance.getX(), entityinstance.getY() + 1.0, entityinstance.getZ(), 5, 0.0, 0.0, 0.0, 0.2
                        );
                     }

                     if (world instanceof ServerLevel _levelxxx) {
                        _levelxxx.sendParticles(
                           ParticleTypes.SMOKE, entityinstance.getX(), entityinstance.getY() + 1.0, entityinstance.getZ(), 10, 0.0, 0.0, 0.0, 0.2
                        );
                     }

                     if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null && entityinstance instanceof Mob _entity) {
                        Entity patt0$temp = entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null;
                        if (patt0$temp instanceof LivingEntity _ent) {
                           _entity.setTarget(_ent);
                        }
                     }

                     _serverLevel.addFreshEntity(entityinstance);
                  }
               }

               if (world instanceof ServerLevel _serverLevelx) {
                  Entity entityinstance = ((EntityType)PaleMonarchModEntities.FALSE_KING_1.get())
                     .create(
                        _serverLevelx,
                        null,
                        BlockPos.containing(
                           entity.getX() + Math.cos((entity.getYRot() - 90.0F) * 0.017453277777777776),
                           entity.getY(),
                           entity.getZ() - Math.sin((entity.getYRot() + 90.0F) * 0.017453277777777776)
                        ),
                        MobSpawnType.MOB_SUMMONED,
                        false,
                        false
                     );
                  if (entityinstance != null) {
                     entityinstance.setYRot(world.getRandom().nextFloat() * 360.0F);
                     if (world instanceof ServerLevel _levelxxx) {
                        _levelxxx.sendParticles(
                           ParticleTypes.LARGE_SMOKE, entityinstance.getX(), entityinstance.getY() + 1.0, entityinstance.getZ(), 5, 0.0, 0.0, 0.0, 0.2
                        );
                     }

                     if (world instanceof ServerLevel _levelxxx) {
                        _levelxxx.sendParticles(
                           ParticleTypes.SMOKE, entityinstance.getX(), entityinstance.getY() + 1.0, entityinstance.getZ(), 10, 0.0, 0.0, 0.0, 0.2
                        );
                     }

                     if ((entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null) != null && entityinstance instanceof Mob _entityx) {
                        Entity patt0$temp = entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null;
                        if (patt0$temp instanceof LivingEntity _ent) {
                           _entityx.setTarget(_ent);
                        }
                     }

                     _serverLevelx.addFreshEntity(entityinstance);
                  }
               }
            }
         );
      }
   }
}
