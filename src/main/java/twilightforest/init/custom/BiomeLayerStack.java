package twilightforest.init.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import twilightforest.TFRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;
import twilightforest.world.components.layer.*;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerType;
import twilightforest.world.components.layer.vanillalegacy.SmoothLayer;
import twilightforest.world.components.layer.vanillalegacy.ZoomLayer;
import twilightforest.world.registration.biomes.BiomeMaker;

import java.util.List;
import java.util.Optional;


public class BiomeLayerStack {
	public static final Codec<BiomeLayerFactory> DISPATCH_CODEC = BiomeLayerTypes.CODEC.dispatch("layer_type", BiomeLayerFactory::getType, BiomeLayerType::getCodec);
	public static final Codec<Holder<BiomeLayerFactory>> HOLDER_CODEC = RegistryFileCodec.create(TFRegistries.Keys.BIOME_STACK, BiomeLayerStack.DISPATCH_CODEC, true);

	public static final ResourceKey<BiomeLayerFactory> RANDOM_FOREST_BIOMES = registerKey("random_forest_biomes");
	public static final ResourceKey<BiomeLayerFactory> BIOMES_ALONG_STREAMS = registerKey("biomes_along_streams");

	public static final ResourceKey<BiomeDensitySource> BIOME_GRID = ResourceKey.create(TFRegistries.Keys.BIOME_TERRAIN_DATA, TwilightForestMod.prefix("biome_grid"));

	public static ResourceKey<BiomeLayerFactory> registerKey(String name) {
		return ResourceKey.create(TFRegistries.Keys.BIOME_STACK, TwilightForestMod.prefix(name));
	}

	public static void bootstrap(BootstrapContext<BiomeLayerFactory> context) {
		List<ResourceKey<Biome>> commonBiomes = ImmutableList.of(
			TFBiomes.FOREST,
			TFBiomes.DENSE_FOREST,
			TFBiomes.MUSHROOM_FOREST,
			TFBiomes.OAK_SAVANNAH,
			TFBiomes.FIREFLY_FOREST,
			TFBiomes.MANGROVE_SWAMP
		);
		BiomeLayerFactory biomes = new RandomBiomeLayer.Factory(1L, 15, commonBiomes, ImmutableList.of(
			TFBiomes.LAKE,
			TFBiomes.DENSE_MUSHROOM_FOREST,
			TFBiomes.LAKE, // Former enchanted forest slot, enchanted forests are now islands inside lakes (LandmarkIslandLayer)
			TFBiomes.CLEARING
		));

		biomes = new KeyBiomesLayer.Factory(1000L, List.of(TFBiomes.GLACIER, TFBiomes.FIRE_SWAMP, TFBiomes.DARK_FOREST_CENTER, TFBiomes.SPOOKY_FOREST), Holder.direct(biomes));
		biomes = new CompanionBiomesLayer.Factory(1000L, List.of(
			Pair.of(TFBiomes.FIRE_SWAMP, TFBiomes.SWAMP),
			Pair.of(TFBiomes.GLACIER, TFBiomes.SNOWY_FOREST),
			Pair.of(TFBiomes.DARK_FOREST_CENTER, TFBiomes.DARK_FOREST),
			Pair.of(TFBiomes.SPOOKY_FOREST, TFBiomes.HIGHLANDS)
		), Holder.direct(biomes));
		// Key slot 4 holds one of several biomes, picked per key biome cell
		biomes = new KeyBiomeVariantsLayer.Factory(1010L, TFBiomes.SPOOKY_FOREST, List.of(TFBiomes.SPOOKY_FOREST, TFBiomes.AMETHYST_STRAITS), Holder.direct(biomes));

		biomes = new ZoomLayer.Factory(1000L, false, Holder.direct(biomes));
		biomes = new ZoomLayer.Factory(1001L, false, Holder.direct(biomes));

		biomes = new StabilizeLayer.Factory(700L, Holder.direct(biomes));

		// Make room for large structures inside the key biome slot formerly held by the Final Plateau
		biomes = new ExpandBiomeLayer.Factory(600L, List.of(TFBiomes.SPOOKY_FOREST, TFBiomes.AMETHYST_STRAITS), Holder.direct(biomes));

		biomes = new BorderLayer.Factory(500L, TFBiomes.SPOOKY_FOREST, TFBiomes.THORNLANDS, Holder.direct(biomes));
		biomes = new BorderLayer.Factory(501L, TFBiomes.AMETHYST_STRAITS, TFBiomes.THORNLANDS, Holder.direct(biomes));

		// Larger lakes: grow by one pixel (64 blocks), only into the common random biomes so key biomes and their rings stay intact
		List<ResourceKey<Biome>> lakeGrowsInto = ImmutableList.<ResourceKey<Biome>>builder().addAll(commonBiomes).add(TFBiomes.DENSE_MUSHROOM_FOREST).add(TFBiomes.CLEARING).build();
		biomes = new ExpandBiomeLayer.Factory(602L, List.of(TFBiomes.LAKE), Optional.of(lakeGrowsInto), Holder.direct(biomes));

		biomes = new ZoomLayer.Factory(1002L, false, Holder.direct(biomes));
		// Smaller clearings: lose 32 + 16 blocks on every side
		biomes = new ShrinkBiomeLayer.Factory(800L, List.of(TFBiomes.CLEARING), Holder.direct(biomes));
		// Grow the amethyst straits by another half pixel (32 blocks) into the inner half of its thornlands ring, the outer ring edge stays where it is
		biomes = new ExpandBiomeLayer.Factory(601L, List.of(TFBiomes.AMETHYST_STRAITS), Optional.of(List.of(TFBiomes.THORNLANDS)), Holder.direct(biomes));
		biomes = new ZoomLayer.Factory(1003L, false, Holder.direct(biomes));
		biomes = new ShrinkBiomeLayer.Factory(801L, List.of(TFBiomes.CLEARING), Holder.direct(biomes));
		biomes = new ZoomLayer.Factory(1004L, false, Holder.direct(biomes));
		biomes = new ZoomLayer.Factory(1005L, false, Holder.direct(biomes));

		// Enchanted forests only exist as islands on the landmark centers of lakes, carrying the quest grove
		biomes = new LandmarkIslandLayer.Factory(900L, TFBiomes.LAKE, TFBiomes.ENCHANTED_FOREST, 56, Optional.empty(), Holder.direct(biomes));
		// Every other darkwood landmark center becomes a small clearing carrying the arachnid dwelling, the rest keep the knight stronghold
		biomes = new LandmarkIslandLayer.Factory(901L, TFBiomes.DARK_FOREST, TFBiomes.DARKWOOD_CLEARING, 40, Optional.of(1), Holder.direct(biomes));

		Holder.Reference<BiomeLayerFactory> randomBiomes = context.register(RANDOM_FOREST_BIOMES, biomes);

		BiomeLayerFactory riverLayer = new SeamLayer.Factory(1L, TFBiomes.STREAM, List.of(TFBiomes.LAKE, TFBiomes.ENCHANTED_FOREST, TFBiomes.DARKWOOD_CLEARING, TFBiomes.THORNLANDS, TFBiomes.CLEARING, TFBiomes.OAK_SAVANNAH), List.of(
			Pair.of(TFBiomes.SNOWY_FOREST, TFBiomes.GLACIER),
			Pair.of(TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST),
			Pair.of(TFBiomes.SWAMP, TFBiomes.FIRE_SWAMP),
			Pair.of(TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER),
			Pair.of(TFBiomes.HIGHLANDS, TFBiomes.SPOOKY_FOREST),
			Pair.of(TFBiomes.HIGHLANDS, TFBiomes.AMETHYST_STRAITS)
		), randomBiomes);
		riverLayer = new SmoothLayer.Factory(7000L, Holder.direct(riverLayer));

		context.register(BIOMES_ALONG_STREAMS, new FilteredBiomeLayer.Factory(100L, TFBiomes.STREAM, Holder.direct(riverLayer), randomBiomes));
	}

	public static void bootstrapData(BootstrapContext<BiomeDensitySource> context) {
		HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);

		context.register(BIOME_GRID, new BiomeDensitySource(
			BiomeMaker.makeBiomeList(biomeRegistry, biomeRegistry.getOrThrow(TFBiomes.UNDERGROUND)),
			context.lookup(TFRegistries.Keys.BIOME_STACK).getOrThrow(BiomeLayerStack.BIOMES_ALONG_STREAMS)
		));
	}
}
