package twilightforest.ivarsbosses;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.ivarsbosses.client.IvarsBossesClient;

/**
 * Bosses ported from Ivar's Bosses (ferocious_creature 1.0.0). Only Fenrir so far; it spawns in the amethyst straits ruin towns.
 * The summon rituals and the login promo message of the original mod are not ported.
 */
public class IvarsBosses {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, TwilightForestMod.ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TwilightForestMod.ID);
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, TwilightForestMod.ID);

	public static final DeferredHolder<EntityType<?>, EntityType<FenrirEntity>> FENRIR = ENTITIES.register("fenrir",
		() -> EntityType.Builder.of(FenrirEntity::new, MobCategory.MONSTER).sized(3.0F, 3.0F).clientTrackingRange(18).updateInterval(3).build("fenrir"));
	public static final DeferredItem<Item> FENRIR_SPAWN_EGG = ITEMS.register("fenrir_spawn_egg",
		() -> new DeferredSpawnEggItem(FENRIR, 0xD65EA3, 0xF4D8E8, new Item.Properties()));

	public static final DeferredHolder<SoundEvent, SoundEvent> FENRIR_AWAKENING = sound("entity.twilightforest.fenrir.awakening");
	public static final DeferredHolder<SoundEvent, SoundEvent> FENRIR_HURT = sound("entity.twilightforest.fenrir.hurt");
	public static final DeferredHolder<SoundEvent, SoundEvent> FENRIR_DEATH = sound("entity.twilightforest.fenrir.death");

	// Fenrir's crystal pillars and shards are block displays it removes itself; any that got saved with an unloading chunk are dropped on load
	public static final String FENRIR_EFFECT_TAG = "twilightforest.fenrir_effect";

	private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
		return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TwilightForestMod.prefix(name)));
	}

	public static void register(IEventBus modBus) {
		ENTITIES.register(modBus);
		ITEMS.register(modBus);
		SOUNDS.register(modBus);
		modBus.addListener(IvarsBosses::registerAttributes);
		modBus.addListener(IvarsBosses::addToTabs);
		NeoForge.EVENT_BUS.addListener(IvarsBosses::dropSavedEffects);
		if (FMLEnvironment.dist.isClient()) {
			modBus.addListener(IvarsBossesClient::registerEntityRenderers);
		}
	}

	private static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(FENRIR.get(), FenrirEntity.createAttributes().build());
	}

	private static void dropSavedEffects(EntityJoinLevelEvent event) {
		if (event.loadedFromDisk() && event.getEntity() instanceof Display && event.getEntity().getTags().contains(FENRIR_EFFECT_TAG)) {
			event.setCanceled(true);
		}
	}

	private static void addToTabs(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(FENRIR_SPAWN_EGG.get());
		}
	}
}
