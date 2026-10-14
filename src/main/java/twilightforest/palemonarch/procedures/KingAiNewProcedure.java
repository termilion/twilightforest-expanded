package twilightforest.palemonarch.procedures;

import java.util.Comparator;
import twilightforest.palemonarch.entity.PaleMonarchEntity;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class KingAiNewProcedure {
   public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
      if (entity != null) {
         if (!world.isClientSide()) {
            if ((entity instanceof PaleMonarchEntity _datEntI ? (Integer)_datEntI.getEntityData().get(PaleMonarchEntity.DATA_cooldown) : 0) > 0
               && entity instanceof PaleMonarchEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     PaleMonarchEntity.DATA_cooldown,
                     (entity instanceof PaleMonarchEntity _datEntIx ? (Integer)_datEntIx.getEntityData().get(PaleMonarchEntity.DATA_cooldown) : 0) - 1
                  );
            }

            if ((entity instanceof PaleMonarchEntity _datEntI ? (Integer)_datEntI.getEntityData().get(PaleMonarchEntity.DATA_attackcooldown) : 0) > 0
               && entity instanceof PaleMonarchEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     PaleMonarchEntity.DATA_attackcooldown,
                     (entity instanceof PaleMonarchEntity _datEntIx ? (Integer)_datEntIx.getEntityData().get(PaleMonarchEntity.DATA_attackcooldown) : 0) - 1
                  );
            }

            if ((entity instanceof PaleMonarchEntity _datEntI ? (Integer)_datEntI.getEntityData().get(PaleMonarchEntity.DATA_dashcooldown) : 0) > 0
               && entity instanceof PaleMonarchEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     PaleMonarchEntity.DATA_dashcooldown,
                     (entity instanceof PaleMonarchEntity _datEntIx ? (Integer)_datEntIx.getEntityData().get(PaleMonarchEntity.DATA_dashcooldown) : 0) - 1
                  );
            }

            if ((entity instanceof PaleMonarchEntity _datEntI ? (Integer)_datEntI.getEntityData().get(PaleMonarchEntity.DATA_clonecooldown) : 0) > 0
               && entity instanceof PaleMonarchEntity _datEntSetI) {
               _datEntSetI.getEntityData()
                  .set(
                     PaleMonarchEntity.DATA_clonecooldown,
                     (entity instanceof PaleMonarchEntity _datEntIx ? (Integer)_datEntIx.getEntityData().get(PaleMonarchEntity.DATA_clonecooldown) : 0) - 1
                  );
            }

            if ((entity.getDeltaMovement().x() != 0.0 || entity.getDeltaMovement().z() != 0.0)
               && (entity instanceof PaleMonarchEntity _datEntI ? (Integer)_datEntI.getEntityData().get(PaleMonarchEntity.DATA_cooldown) : 0) <= 0) {
               if (entity instanceof PaleMonarchEntity) {
                  ((PaleMonarchEntity)entity).setAnimation("Slow Walk");
               }
            } else if (entity.getDeltaMovement().x() == 0.0
               && entity.getDeltaMovement().z() == 0.0
               && ((PaleMonarchEntity)entity).animationprocedure.equals("Slow Walk")
               && entity instanceof PaleMonarchEntity) {
               ((PaleMonarchEntity)entity).setAnimation("empty");
            }

            if ((entity instanceof Mob _mobEnt ? _mobEnt.getTarget() : null) != null) {
               if (entity instanceof PaleMonarchEntity _datEntSetI) {
                  _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_downtimebeforeheal, 60);
               }

               if ((entity instanceof PaleMonarchEntity _datEntIx ? (Integer)_datEntIx.getEntityData().get(PaleMonarchEntity.DATA_cooldown) : 0) <= 0) {
                  if (entity instanceof Mob _entity) {
                     _entity.getNavigation()
                        .moveTo(
                           (entity instanceof Mob _mobEntxxx ? _mobEntxxx.getTarget() : null).getX(),
                           (entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null).getY(),
                           (entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null).getZ(),
                           1.0
                        );
                  }
               } else if (entity instanceof Mob _entity) {
                  _entity.getNavigation().stop();
               }

               if ((entity instanceof PaleMonarchEntity _datEntIxx ? (Integer)_datEntIxx.getEntityData().get(PaleMonarchEntity.DATA_attackcooldown) : 0) < 0) {
                  if (entity instanceof LivingEntity _entity) {
                     AttributeInstance _attrInst = _entity.getAttribute(Attributes.STEP_HEIGHT);
                     if (_attrInst != null) {
                        _attrInst.setBaseValue(1.0);
                     }
                  }
               } else if (entity instanceof LivingEntity _entityx) {
                  AttributeInstance _attrInst = _entityx.getAttribute(Attributes.STEP_HEIGHT);
                  if (_attrInst != null) {
                     _attrInst.setBaseValue(0.6F);
                  }
               }

               Vec3 _center = new Vec3(x, y, z);

               for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiterator == (entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null)
                     && (entity instanceof PaleMonarchEntity _datEntIxxxxx ? (Integer)_datEntIxxxxx.getEntityData().get(PaleMonarchEntity.DATA_cooldown) : 0)
                        <= 0
                     && (
                           entity instanceof PaleMonarchEntity _datEntIxxxx
                              ? (Integer)_datEntIxxxx.getEntityData().get(PaleMonarchEntity.DATA_attackcooldown)
                              : 0
                        )
                        <= 0
                     && (
                           entity instanceof PaleMonarchEntity _datEntIxxx
                              ? (Integer)_datEntIxxx.getEntityData().get(PaleMonarchEntity.DATA_AttacksUntilBuff)
                              : 0
                        )
                        > 7) {
                     if (entity instanceof PaleMonarchEntity) {
                        ((PaleMonarchEntity)entity).setAnimation("empty");
                     }

                     if (entity instanceof PaleMonarchEntity _datEntSetI) {
                        _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 60);
                     }

                     if (entity instanceof PaleMonarchEntity _datEntSetI) {
                        _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_AttacksUntilBuff, 0);
                     }

                     KingBuffingProcedure.execute(world, x, y, z, entity);
                  }
               }


               for (Entity entityiteratorx : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(1.75), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if (entityiteratorx == (entity instanceof Mob _mobEntx ? _mobEntx.getTarget() : null)
                     && (entity instanceof PaleMonarchEntity _datEntIxxxx ? (Integer)_datEntIxxxx.getEntityData().get(PaleMonarchEntity.DATA_cooldown) : 0)
                        <= 0
                     && (entity instanceof PaleMonarchEntity _datEntIxxx ? (Integer)_datEntIxxx.getEntityData().get(PaleMonarchEntity.DATA_attackcooldown) : 0)
                        <= 0) {
                     if (entity instanceof LivingEntity _entityxx
                        && _entityxx.hasLineOfSight(entity instanceof Mob _mobEntxx ? _mobEntxx.getTarget() : null)
                        && (
                              entity instanceof PaleMonarchEntity _datEntIxxxxx
                                 ? (Integer)_datEntIxxxxx.getEntityData().get(PaleMonarchEntity.DATA_clonecooldown)
                                 : 0
                           )
                           <= 0
                        && entity instanceof PaleMonarchEntity _datEntL52
                        && (Boolean)_datEntL52.getEntityData().get(PaleMonarchEntity.DATA_canrangeduse)
                        && (entity instanceof LivingEntity _livEntx ? _livEntx.getHealth() : -1.0F)
                           <= (entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1.0F) * 0.75) {
                        if (Mth.nextInt(RandomSource.create(), 1, 6) == 1) {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 60);
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_clonecooldown, Mth.nextInt(RandomSource.create(), 200, 480));
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetL) {
                              _datEntSetL.getEntityData().set(PaleMonarchEntity.DATA_canrangeduse, false);
                           }

                           SummonFalseKing1Procedure.execute(world, x, y, z, entity);
                        } else {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 60);
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_clonecooldown, Mth.nextInt(RandomSource.create(), 160, 280));
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetL) {
                              _datEntSetL.getEntityData().set(PaleMonarchEntity.DATA_canrangeduse, false);
                           }

                           SummonFalseKing2Procedure.execute(world, x, y, z, entity);
                        }
                     } else if (Mth.nextInt(RandomSource.create(), 1, 6) == 1
                        && entity instanceof PaleMonarchEntity _datEntL67
                        && (Boolean)_datEntL67.getEntityData().get(PaleMonarchEntity.DATA_canrangeduse)
                        && (entity instanceof LivingEntity _livEntxxx ? _livEntxxx.getHealth() : -1.0F)
                           <= (entity instanceof LivingEntity _livEntxx ? _livEntxx.getMaxHealth() : -1.0F) * 0.75) {
                        if (entity instanceof PaleMonarchEntity) {
                           ((PaleMonarchEntity)entity).setAnimation("empty");
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 50);
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_attackcooldown, 50);
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_dashcooldown, Mth.nextInt(RandomSource.create(), 140, 280));
                        }

                        JumpafterpunchProcedure.execute(world, x, y, z, entity);
                     } else {
                        if (entity instanceof PaleMonarchEntity) {
                           ((PaleMonarchEntity)entity).setAnimation("empty");
                        }

                        if (Mth.nextInt(RandomSource.create(), 1, 2) == 1 && entity instanceof PaleMonarchEntity _datEntSetL) {
                           _datEntSetL.getEntityData().set(PaleMonarchEntity.DATA_canrangeduse, true);
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData()
                              .set(
                                 PaleMonarchEntity.DATA_AttacksUntilBuff,
                                 (
                                       entity instanceof PaleMonarchEntity _datEntIxxxxxx
                                          ? (Integer)_datEntIxxxxxx.getEntityData().get(PaleMonarchEntity.DATA_AttacksUntilBuff)
                                          : 0
                                    )
                                    + 1
                              );
                        }

                        if (entity instanceof PaleMonarchEntity _datEntL80 && (Boolean)_datEntL80.getEntityData().get(PaleMonarchEntity.DATA_IsBuffed)) {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 240);
                           }

                           BuffedMultiSwingProcedure.execute(world, x, y, z, entity);
                        } else if (Mth.nextInt(RandomSource.create(), 1, 5) == 1) {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 60);
                           }

                           DoubleSwingProcedure.execute(world, x, y, z, entity);
                        } else if (Mth.nextInt(RandomSource.create(), 1, 5) == 1) {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 100);
                           }

                           MultiSwingProcedure.execute(world, x, y, z, entity);
                        } else if (Mth.nextInt(RandomSource.create(), 1, 3) == 1) {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (entity instanceof PaleMonarchEntity _datEntSetI) {
                              _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 45);
                           }

                           GiantSwingProcedure.execute(world, x, y, z, entity);
                        } else if (Mth.nextInt(RandomSource.create(), 1, 2) == 1) {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (Mth.nextInt(RandomSource.create(), 1, 2) == 1) {
                              if (entity instanceof PaleMonarchEntity _datEntSetI) {
                                 _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 40);
                              }

                              NormalOverheadJumpSwingProcedure.execute(world, x, y, z, entity);
                           } else {
                              if (entity instanceof PaleMonarchEntity _datEntSetI) {
                                 _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 20);
                              }

                              OverheadSwingProcedure.execute(world, x, y, z, entity);
                           }
                        } else {
                           if (entity instanceof PaleMonarchEntity) {
                              ((PaleMonarchEntity)entity).setAnimation("empty");
                           }

                           if (Mth.nextInt(RandomSource.create(), 1, 2) == 1) {
                              if (entity instanceof PaleMonarchEntity _datEntSetI) {
                                 _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 20);
                              }

                              CrossSwingProcedure.execute(world, x, y, z, entity);
                           } else if (Mth.nextInt(RandomSource.create(), 1, 2) == 1) {
                              if (entity instanceof PaleMonarchEntity _datEntSetI) {
                                 _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 25);
                              }

                              GutPunchProcedure.execute(world, x, y, z, entity);
                           } else if ((entity instanceof LivingEntity _livEntxxx ? _livEntxxx.getHealth() : -1.0F)
                              <= (entity instanceof LivingEntity _livEntxx ? _livEntxx.getMaxHealth() : -1.0F) * 0.75) {
                              if (entity instanceof PaleMonarchEntity _datEntSetI) {
                                 _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 25);
                              }

                              PunchthenJumpswingProcedure.execute(world, x, y, z, entity);
                           } else {
                              if (entity instanceof PaleMonarchEntity _datEntSetI) {
                                 _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 25);
                              }

                              GutPunchProcedure.execute(world, x, y, z, entity);
                           }
                        }
                     }
                  }
               }


               for (Entity entityiteratorxx : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(3.0), e -> true)
                  .stream()
                  .sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
                  .toList()) {
                  if ((entity instanceof PaleMonarchEntity _datEntIxxxxxxx ? (Integer)_datEntIxxxxxxx.getEntityData().get(PaleMonarchEntity.DATA_cooldown) : 0)
                        <= 0
                     && (
                           entity instanceof PaleMonarchEntity _datEntIxxxxxx
                              ? (Integer)_datEntIxxxxxx.getEntityData().get(PaleMonarchEntity.DATA_attackcooldown)
                              : 0
                        )
                        <= 0
                     && !(entity instanceof PaleMonarchEntity _datEntL109 && (Boolean)_datEntL109.getEntityData().get(PaleMonarchEntity.DATA_IsBuffed))) {
                     if (Mth.nextInt(RandomSource.create(), 1, 3) <= 2
                        && (
                              entity instanceof PaleMonarchEntity _datEntIxxxxxxxx
                                 ? (Integer)_datEntIxxxxxxxx.getEntityData().get(PaleMonarchEntity.DATA_dashcooldown)
                                 : 0
                           )
                           <= 0) {
                        if (entity instanceof PaleMonarchEntity) {
                           ((PaleMonarchEntity)entity).setAnimation("empty");
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 60);
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_attackcooldown, 60);
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_dashcooldown, Mth.nextInt(RandomSource.create(), 140, 280));
                        }

                        JumpafterpunchProcedure.execute(world, x, y, z, entity);
                     } else if (entity instanceof LivingEntity _entityxxx
                        && _entityxxx.hasLineOfSight(entity instanceof Mob _mobEntxxx ? _mobEntxxx.getTarget() : null)
                        && (
                              entity instanceof PaleMonarchEntity _datEntIxxxxxxxx
                                 ? (Integer)_datEntIxxxxxxxx.getEntityData().get(PaleMonarchEntity.DATA_clonecooldown)
                                 : 0
                           )
                           <= 0
                        && (entity instanceof LivingEntity _livEntxxx ? _livEntxxx.getHealth() : -1.0F)
                           <= (entity instanceof LivingEntity _livEntxx ? _livEntxx.getMaxHealth() : -1.0F) * 0.75
                        && entity instanceof PaleMonarchEntity _datEntL122
                        && (Boolean)_datEntL122.getEntityData().get(PaleMonarchEntity.DATA_canrangeduse)) {
                        if (entity instanceof PaleMonarchEntity) {
                           ((PaleMonarchEntity)entity).setAnimation("empty");
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_cooldown, 60);
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetI) {
                           _datEntSetI.getEntityData().set(PaleMonarchEntity.DATA_clonecooldown, Mth.nextInt(RandomSource.create(), 200, 480));
                        }

                        if (entity instanceof PaleMonarchEntity _datEntSetL) {
                           _datEntSetL.getEntityData().set(PaleMonarchEntity.DATA_canrangeduse, false);
                        }

                        SummonFalseKing1Procedure.execute(world, x, y, z, entity);
                     }
                  }
               }
            } else if ((
                  entity instanceof PaleMonarchEntity _datEntIxxxxxxxx
                     ? (Integer)_datEntIxxxxxxxx.getEntityData().get(PaleMonarchEntity.DATA_downtimebeforeheal)
                     : 0
               )
               > 0) {
               if (entity instanceof PaleMonarchEntity _datEntSetI) {
                  _datEntSetI.getEntityData()
                     .set(
                        PaleMonarchEntity.DATA_downtimebeforeheal,
                        (
                              entity instanceof PaleMonarchEntity _datEntIxxxxxxxxx
                                 ? (Integer)_datEntIxxxxxxxxx.getEntityData().get(PaleMonarchEntity.DATA_downtimebeforeheal)
                                 : 0
                           )
                           - 1
                     );
               }
            } else if (entity instanceof LivingEntity _entityxxx && !_entityxxx.level().isClientSide()) {
               _entityxxx.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 0));
            }
         }
      }
   }
}
