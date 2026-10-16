package twilightforest.compat.mythsandlegends;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import twilightforest.init.TFDimension;

/**
 * Myths & Legends only spawns its mobs, biome and structures in the Twilight Forest (the chronicler and his house stay in the overworld).
 * Its overworld/nether biome modifiers and structure biome tags are overridden by data files in data/mythsandlegends.
 */
public class MythsAndLegendsCompat {
	public static final String MOD_ID = "mythsandlegends";

	// The alp reaches the mangrove swamp through Myths & Legends' #c:is_swamp spawns, now replaced by our own mangrove modifier.
	// Mangrove swamps also generate in the overworld, so natural alp spawns are limited to the Twilight Forest here.
	public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
		BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(MOD_ID, "alp")).ifPresent(alp ->
			event.register(alp, null, null, (type, level, spawnType, pos, random) -> spawnType != MobSpawnType.NATURAL || TFDimension.isTwilightPortalDestination(level.getLevel()), RegisterSpawnPlacementsEvent.Operation.AND));
	}

	// Myths & Legends registers its umbral wasteland TerraBlender region during client setup, which has finished by now
	public static void removeOverworldBiome(FMLLoadCompleteEvent event) {
		if (ModList.get().isLoaded("terrablender")) {
			event.enqueueWork(TerraBlenderCompat::removeMythsAndLegendsRegion);
		}
	}
}
