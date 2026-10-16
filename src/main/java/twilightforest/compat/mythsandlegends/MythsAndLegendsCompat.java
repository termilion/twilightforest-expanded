package twilightforest.compat.mythsandlegends;

import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Myths & Legends only spawns its mobs, biome and structures in the Twilight Forest (the chronicler and his house stay in the overworld).
 * Its overworld/nether biome modifiers and structure biome tags are overridden by data files in data/mythsandlegends.
 */
public class MythsAndLegendsCompat {
	public static final String MOD_ID = "mythsandlegends";

	// Myths & Legends registers its umbral wasteland TerraBlender region during client setup, which has finished by now
	public static void removeOverworldBiome(FMLLoadCompleteEvent event) {
		if (ModList.get().isLoaded("terrablender")) {
			event.enqueueWork(TerraBlenderCompat::removeMythsAndLegendsRegion);
		}
	}
}
