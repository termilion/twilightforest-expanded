package twilightforest.compat.mythsandlegends;

import net.minecraft.resources.ResourceLocation;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

// Only loaded when TerraBlender is present
public class TerraBlenderCompat {
	public static void removeMythsAndLegendsRegion() {
		ResourceLocation region = ResourceLocation.fromNamespaceAndPath(MythsAndLegendsCompat.MOD_ID, "overworld_1");
		if (Regions.get(RegionType.OVERWORLD).stream().anyMatch(r -> r.getName().equals(region))) {
			Regions.remove(RegionType.OVERWORLD, region);
		}
		SurfaceRuleManager.removeSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MythsAndLegendsCompat.MOD_ID);
	}
}
