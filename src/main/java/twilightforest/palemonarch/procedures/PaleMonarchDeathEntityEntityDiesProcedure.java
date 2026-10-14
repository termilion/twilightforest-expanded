package twilightforest.palemonarch.procedures;

import twilightforest.palemonarch.PaleMonarchMod;
import twilightforest.palemonarch.entity.PaleMonarchDeathEntityEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class PaleMonarchDeathEntityEntityDiesProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (entity instanceof PaleMonarchDeathEntityEntity) {
            ((PaleMonarchDeathEntityEntity)entity).setAnimation("empty");
         }

         if (entity instanceof PaleMonarchDeathEntityEntity) {
            ((PaleMonarchDeathEntityEntity)entity).setAnimation("newDeath");
         }

         if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
               _level.playSound(
                  null,
                  BlockPos.containing(x, y, z),
                  (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.zombie.death")),
                  SoundSource.NEUTRAL,
                  0.5F,
                  0.5F
               );
            } else {
               _level.playLocalSound(
                  x, y, z, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.zombie.death")), SoundSource.NEUTRAL, 0.5F, 0.5F, false
               );
            }
         }

         if (world instanceof Level _levelx) {
            if (!_levelx.isClientSide()) {
               _levelx.playSound(
                  null,
                  BlockPos.containing(x, y, z),
                  (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.wither.death")),
                  SoundSource.NEUTRAL,
                  0.1F,
                  0.5F
               );
            } else {
               _levelx.playLocalSound(
                  x, y, z, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.wither.death")), SoundSource.NEUTRAL, 0.1F, 0.5F, false
               );
            }
         }

         if (world instanceof Level _levelxx) {
            if (!_levelxx.isClientSide()) {
               _levelxx.playSound(
                  null,
                  BlockPos.containing(x, y, z),
                  (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.endermite.death")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  0.25F
               );
            } else {
               _levelxx.playLocalSound(
                  x,
                  y,
                  z,
                  (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.endermite.death")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  0.25F,
                  false
               );
            }
         }

         if (world instanceof Level _levelxxx) {
            if (!_levelxxx.isClientSide()) {
               _levelxxx.playSound(
                  null,
                  BlockPos.containing(x, y, z),
                  (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.bell.use")),
                  SoundSource.NEUTRAL,
                  1.0F,
                  0.5F
               );
            } else {
               _levelxxx.playLocalSound(
                  x, y, z, (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.bell.use")), SoundSource.NEUTRAL, 1.0F, 0.5F, false
               );
            }
         }

         PaleMonarchMod.queueServerWork(
            25,
            () -> {
               if (world instanceof Level _levelxxxxx) {
                  if (!_levelxxxxx.isClientSide()) {
                     _levelxxxxx.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("block.candle.extinguish")),
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F
                     );
                  } else {
                     _levelxxxxx.playLocalSound(
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

               if (world instanceof Level _levelxxxx) {
                  if (!_levelxxxx.isClientSide()) {
                     _levelxxxx.playSound(
                        null,
                        BlockPos.containing(x, y, z),
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.dragon_fireball.explode")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F
                     );
                  } else {
                     _levelxxxx.playLocalSound(
                        x,
                        y,
                        z,
                        (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.dragon_fireball.explode")),
                        SoundSource.NEUTRAL,
                        0.5F,
                        1.0F,
                        false
                     );
                  }
               }

               if (world instanceof ServerLevel _levelxxxxxx) {
                  _levelxxxxxx.sendParticles(
                     ParticleTypes.SMOKE,
                     x,
                     y + 1.0,
                     z,
                     10,
                     Mth.nextInt(RandomSource.create(), 0, 0),
                     Mth.nextInt(RandomSource.create(), 0, 0),
                     Mth.nextInt(RandomSource.create(), 0, 0),
                     0.2
                  );
               }

               if (world instanceof ServerLevel _levelxxxxxx) {
                  _levelxxxxxx.sendParticles(
                     ParticleTypes.LARGE_SMOKE,
                     x,
                     y + 1.0,
                     z,
                     5,
                     Mth.nextInt(RandomSource.create(), 0, 0),
                     Mth.nextInt(RandomSource.create(), 0, 0),
                     Mth.nextInt(RandomSource.create(), 0, 0),
                     0.2
                  );
               }

               if (world instanceof ServerLevel _levelxxxxxx) {
                  ExperienceOrb.award(_levelxxxxxx, new Vec3(x, y, z), 7500);
               }

               if (!entity.level().isClientSide()) {
                  entity.discard();
               }
            }
         );
      }
   }
}
