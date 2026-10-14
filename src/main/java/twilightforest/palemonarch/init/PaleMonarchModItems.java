package twilightforest.palemonarch.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.palemonarch.item.PaleAxeItem;

public class PaleMonarchModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(TwilightForestMod.ID);

	public static final DeferredItem<Item> PALE_AXE = REGISTRY.register("pale_axe", PaleAxeItem::new);
	public static final DeferredItem<Item> PALE_MONARCH_SPAWN_EGG = REGISTRY.register("pale_monarch_spawn_egg",
		() -> new DeferredSpawnEggItem(PaleMonarchModEntities.PALE_MONARCH, 0xe6e1d3, 0x3a3a3a, new Item.Properties()));
}
