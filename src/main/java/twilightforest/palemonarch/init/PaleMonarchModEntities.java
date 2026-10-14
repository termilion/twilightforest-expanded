package twilightforest.palemonarch.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.palemonarch.entity.FalseKing1Entity;
import twilightforest.palemonarch.entity.FalseKing2Entity;
import twilightforest.palemonarch.entity.PaleMonarchDeathEntityEntity;
import twilightforest.palemonarch.entity.PaleMonarchEntity;
import twilightforest.palemonarch.entity.ShadowEntity;

// Ported from the Pale Monarch mod (pale_monarch 1.1.0, MCreator). Registry names live in the twilightforest namespace.
public class PaleMonarchModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, TwilightForestMod.ID);

	public static final DeferredHolder<EntityType<?>, EntityType<PaleMonarchEntity>> PALE_MONARCH = register("pale_monarch",
		EntityType.Builder.of(PaleMonarchEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).sized(0.6F, 1.8F));
	public static final DeferredHolder<EntityType<?>, EntityType<PaleMonarchDeathEntityEntity>> PALE_MONARCH_DEATH_ENTITY = register("pale_monarch_death",
		EntityType.Builder.of(PaleMonarchDeathEntityEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).sized(0.6F, 1.8F).noSummon());
	public static final DeferredHolder<EntityType<?>, EntityType<FalseKing1Entity>> FALSE_KING_1 = register("false_king_1",
		EntityType.Builder.of(FalseKing1Entity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6F, 1.8F));
	public static final DeferredHolder<EntityType<?>, EntityType<FalseKing2Entity>> FALSE_KING_2 = register("false_king_2",
		EntityType.Builder.of(FalseKing2Entity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6F, 1.8F));
	public static final DeferredHolder<EntityType<?>, EntityType<ShadowEntity>> SHADOW = register("king_shadow",
		EntityType.Builder.of(ShadowEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().sized(0.6F, 1.8F));

	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> builder) {
		return REGISTRY.register(name, () -> builder.build(name));
	}

	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(PALE_MONARCH.get(), PaleMonarchEntity.createAttributes().build());
		event.put(PALE_MONARCH_DEATH_ENTITY.get(), PaleMonarchDeathEntityEntity.createAttributes().build());
		event.put(FALSE_KING_1.get(), FalseKing1Entity.createAttributes().build());
		event.put(FALSE_KING_2.get(), FalseKing2Entity.createAttributes().build());
		event.put(SHADOW.get(), ShadowEntity.createAttributes().build());
	}
}
