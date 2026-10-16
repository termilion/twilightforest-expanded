package twilightforest.compat.rottencreatures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import twilightforest.init.TFDimension;

import java.util.List;

/**
 * Rotten Creatures' surface mobs need darkness, but the swamp and snowy forest are too bright for them most of the time.
 * In the Twilight Forest they may spawn at any light level (still on valid ground and under open sky like their own rules).
 */
public class RottenCreaturesCompat {
	public static final String MOD_ID = "rottencreatures";
	private static final List<String> ANY_LIGHT_MOBS = List.of("frostbitten", "glacial_hunter", "swampy");

	@SuppressWarnings("unchecked")
	public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
		for (String name : ANY_LIGHT_MOBS) {
			BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.fromNamespaceAndPath(MOD_ID, name)).ifPresent(type ->
				event.register((EntityType<Monster>) type, null, null, (entityType, level, spawnType, pos, random) ->
					TFDimension.isTwilightPortalDestination(level.getLevel()) && Monster.checkAnyLightMonsterSpawnRules(entityType, level, spawnType, pos, random)
						&& (spawnType == MobSpawnType.SPAWNER || level.canSeeSky(pos)), RegisterSpawnPlacementsEvent.Operation.OR));
		}
	}
}
