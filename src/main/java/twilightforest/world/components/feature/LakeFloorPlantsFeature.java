package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

/**
 * Covers every underwater floor column of the chunk (inside the given biome) with kelp or seagrass, unless the floor block is in {@code cannot_grow_on}.
 * Kelp grows in noise patches, seagrass everywhere else. Placed once per chunk, after structures.
 */
public class LakeFloorPlantsFeature extends Feature<LakeFloorPlantsFeature.Config> {
	// Same noise as the (deprecated) Biome.BIOME_INFO_NOISE
	private static final PerlinSimplexNoise PATCH_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), List.of(0));

	public LakeFloorPlantsFeature(Codec<Config> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<Config> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		Config config = context.config();
		int chunkX = context.origin().getX() & ~15;
		int chunkZ = context.origin().getZ() & ~15;
		boolean placed = false;

		// Scan the columns directly: during feature placement the OCEAN_FLOOR heightmap is not updated for blocks that structures just placed
		// Level#getSeaLevel is a constant 63 in 1.21.1, the generator knows the real one
		int surface = context.chunkGenerator().getSeaLevel() - 1;
		for (int x = chunkX; x < chunkX + 16; x++) {
			for (int z = chunkZ; z < chunkZ + 16; z++) {
				if (!level.getBlockState(new BlockPos(x, surface, z)).is(Blocks.WATER))
					continue;
				BlockPos pos = settleFallingBlocks(level, waterBottom(level, new BlockPos(x, surface, z)));
				// Noise biome, not the fuzzed getBiome: matches the biome map exactly, so lake edges get covered too
				if (!level.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(z)).is(config.biome()))
					continue;
				if (config.cannotGrowOn().contains(level.getBlockState(pos.below()).getBlockHolder()))
					continue;

				boolean kelp = PATCH_NOISE.getValue(x / config.kelpPatchSize(), z / config.kelpPatchSize(), false) > config.kelpThreshold();
				placed |= kelp ? placeKelp(level, random, pos) : placeSeagrass(level, random, pos, config.tallSeagrassChance());
			}
		}

		return placed;
	}

	// Lowest water block of the column below the given water block
	private static BlockPos waterBottom(WorldGenLevel level, BlockPos water) {
		BlockPos.MutableBlockPos pos = water.mutable();
		while (pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos.below()).is(Blocks.WATER))
			pos.move(Direction.DOWN);
		return pos.immutable();
	}

	/**
	 * Ruin templates leave sand and gravel hanging over water; it would fall after generation and bury the plants below it.
	 * Drop the hanging stack now, like falling blocks would (suspicious blocks turn plain, their loot is lost), and return the new
	 * lowest water block of the column.
	 */
	private static BlockPos settleFallingBlocks(WorldGenLevel level, BlockPos bottomWater) {
		BlockPos top = bottomWater.below();
		if (!isFallable(level.getBlockState(top)))
			return bottomWater;

		BlockPos bottom = top;
		while (bottom.getY() > level.getMinBuildHeight() && isFallable(level.getBlockState(bottom.below())))
			bottom = bottom.below();
		if (!FallingBlock.isFree(level.getBlockState(bottom.below())))
			return bottomWater;

		int drop = bottom.getY() - waterBottom(level, bottom.below()).getY();
		for (BlockPos from = bottom; from.getY() <= top.getY(); from = from.above()) {
			BlockState state = level.getBlockState(from);
			if (state.getBlock() instanceof BrushableBlock brushable)
				state = brushable.getTurnsInto().defaultBlockState();
			level.setBlock(from, Blocks.WATER.defaultBlockState(), 2);
			level.setBlock(from.below(drop), state, 2);
		}
		return top.below(drop).above();
	}

	private static boolean isFallable(BlockState state) {
		return state.getBlock() instanceof Fallable;
	}

	// Copy of KelpFeature#place for a fixed floor position
	private static boolean placeKelp(WorldGenLevel level, RandomSource random, BlockPos floor) {
		BlockState top = Blocks.KELP.defaultBlockState();
		BlockState plant = Blocks.KELP_PLANT.defaultBlockState();
		int height = 1 + random.nextInt(10);
		BlockPos.MutableBlockPos pos = floor.mutable();

		for (int i = 0; i <= height; i++) {
			if (level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).is(Blocks.WATER) && plant.canSurvive(level, pos)) {
				level.setBlock(pos, i == height ? top.setValue(KelpBlock.AGE, random.nextInt(4) + 20) : plant, 2);
				if (i == height)
					return true;
			} else {
				if (i == 0)
					return false;
				BlockPos below = pos.below();
				if (top.canSurvive(level, below))
					level.setBlock(below, top.setValue(KelpBlock.AGE, random.nextInt(4) + 20), 2);
				return true;
			}
			pos.move(0, 1, 0);
		}
		return true;
	}

	private static boolean placeSeagrass(WorldGenLevel level, RandomSource random, BlockPos pos, float tallChance) {
		BlockState seagrass = Blocks.SEAGRASS.defaultBlockState();
		if (!seagrass.canSurvive(level, pos))
			return false;

		BlockPos above = pos.above();
		if (random.nextFloat() < tallChance && level.getBlockState(above).is(Blocks.WATER)) {
			BlockState tall = Blocks.TALL_SEAGRASS.defaultBlockState();
			level.setBlock(pos, tall, 2);
			level.setBlock(above, tall.setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER), 2);
		} else {
			level.setBlock(pos, seagrass, 2);
		}
		return true;
	}

	public record Config(ResourceKey<Biome> biome, HolderSet<Block> cannotGrowOn, double kelpPatchSize, double kelpThreshold, float tallSeagrassChance) implements FeatureConfiguration {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(Config::biome),
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("cannot_grow_on").forGetter(Config::cannotGrowOn),
			Codec.doubleRange(1, 512).fieldOf("kelp_patch_size").forGetter(Config::kelpPatchSize),
			Codec.doubleRange(-1, 1).fieldOf("kelp_threshold").forGetter(Config::kelpThreshold),
			Codec.floatRange(0, 1).fieldOf("tall_seagrass_chance").forGetter(Config::tallSeagrassChance)
		).apply(instance, Config::new));
	}
}
