package twilightforest.world.components.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.jetbrains.annotations.Nullable;

/**
 * Copy of the vanilla {@link net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature} (basalt deltas) with a configurable block.
 * Columns also never start on water, so they don't float on delta pools.
 */
public class BlockColumnsFeature extends Feature<BlockColumnsFeature.Config> {
	private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(
		Blocks.LAVA,
		Blocks.WATER,
		Blocks.BEDROCK,
		Blocks.MAGMA_BLOCK,
		Blocks.SOUL_SAND,
		Blocks.NETHER_BRICKS,
		Blocks.NETHER_BRICK_FENCE,
		Blocks.NETHER_BRICK_STAIRS,
		Blocks.NETHER_WART,
		Blocks.CHEST,
		Blocks.SPAWNER
	);

	public BlockColumnsFeature(Codec<Config> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<Config> context) {
		int seaLevel = context.chunkGenerator().getSeaLevel();
		BlockPos origin = context.origin();
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		Config config = context.config();
		if (!canPlaceAt(level, seaLevel, origin.mutable())) {
			return false;
		}

		int height = config.height().sample(random);
		boolean clustered = random.nextFloat() < 0.9F;
		int spread = Math.min(height, clustered ? 5 : 8);
		int attempts = clustered ? 50 : 15;
		boolean placed = false;

		for (BlockPos pos : BlockPos.randomBetweenClosed(random, attempts, origin.getX() - spread, origin.getY(), origin.getZ() - spread, origin.getX() + spread, origin.getY(), origin.getZ() + spread)) {
			int columnHeight = height - pos.distManhattan(origin);
			if (columnHeight >= 0) {
				placed |= this.placeColumn(level, seaLevel, pos, columnHeight, config.reach().sample(random), config.state());
			}
		}

		return placed;
	}

	private boolean placeColumn(LevelAccessor level, int seaLevel, BlockPos pos, int distance, int reach, BlockState state) {
		boolean placed = false;

		for (BlockPos target : BlockPos.betweenClosed(pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + reach, pos.getY(), pos.getZ() + reach)) {
			int dist = target.distManhattan(pos);
			BlockPos start = isAirOrLavaOcean(level, seaLevel, target) ? findSurface(level, seaLevel, target.mutable(), dist) : findAir(level, target.mutable(), dist);
			if (start != null) {
				int remaining = distance - dist / 2;

				for (BlockPos.MutableBlockPos column = start.mutable(); remaining >= 0; remaining--) {
					if (isAirOrLavaOcean(level, seaLevel, column)) {
						this.setBlock(level, column, state);
						column.move(Direction.UP);
						placed = true;
					} else {
						if (!level.getBlockState(column).is(state.getBlock())) {
							break;
						}

						column.move(Direction.UP);
					}
				}
			}
		}

		return placed;
	}

	@Nullable
	private static BlockPos findSurface(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos, int distance) {
		while (pos.getY() > level.getMinBuildHeight() + 1 && distance > 0) {
			distance--;
			if (canPlaceAt(level, seaLevel, pos)) {
				return pos;
			}

			pos.move(Direction.DOWN);
		}

		return null;
	}

	private static boolean canPlaceAt(LevelAccessor level, int seaLevel, BlockPos.MutableBlockPos pos) {
		if (!isAirOrLavaOcean(level, seaLevel, pos)) {
			return false;
		}

		BlockState below = level.getBlockState(pos.move(Direction.DOWN));
		pos.move(Direction.UP);
		return !below.isAir() && !CANNOT_PLACE_ON.contains(below.getBlock());
	}

	@Nullable
	private static BlockPos findAir(LevelAccessor level, BlockPos.MutableBlockPos pos, int distance) {
		while (pos.getY() < level.getMaxBuildHeight() && distance > 0) {
			distance--;
			BlockState state = level.getBlockState(pos);
			if (CANNOT_PLACE_ON.contains(state.getBlock())) {
				return null;
			}

			if (state.isAir()) {
				return pos;
			}

			pos.move(Direction.UP);
		}

		return null;
	}

	private static boolean isAirOrLavaOcean(LevelAccessor level, int seaLevel, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.is(Blocks.LAVA) && pos.getY() <= seaLevel;
	}

	/**
	 * Same as {@link net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration}, plus the column block
	 */
	public record Config(BlockState state, IntProvider reach, IntProvider height) implements FeatureConfiguration {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.fieldOf("state").forGetter(Config::state),
			IntProvider.codec(0, 3).fieldOf("reach").forGetter(Config::reach),
			IntProvider.codec(1, 10).fieldOf("height").forGetter(Config::height)
		).apply(instance, Config::new));
	}
}
