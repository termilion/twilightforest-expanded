package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * A bush-like clump of budding amethyst on the ground (replacing grass plants) (one block, sometimes a second one beside or on top of it),
 * with an amethyst bud or cluster growing out of every free side.
 */
public class AmethystBushFeature extends Feature<NoneFeatureConfiguration> {
	private static final Block[] BUDS = {Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER};

	public AmethystBushFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		if (!isFree(level, origin) || !level.getBlockState(origin.below()).is(BlockTags.DIRT))
			return false;

		List<BlockPos> cores = new ArrayList<>();
		cores.add(origin);
		if (random.nextFloat() < 0.4F) {
			BlockPos extra = random.nextBoolean() ? origin.above() : origin.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
			if (isFree(level, extra) && (extra.getY() > origin.getY() || level.getBlockState(extra.below()).is(BlockTags.DIRT)))
				cores.add(extra);
		}

		for (BlockPos core : cores)
			level.setBlock(core, Blocks.BUDDING_AMETHYST.defaultBlockState(), Block.UPDATE_CLIENTS);

		for (BlockPos core : cores) {
			for (Direction side : Direction.values()) {
				BlockPos budPos = core.relative(side);
				if (side == Direction.DOWN || !isFree(level, budPos))
					continue;
				Block bud = BUDS[random.nextInt(BUDS.length)];
				level.setBlock(budPos, bud.defaultBlockState().setValue(AmethystClusterBlock.FACING, side), Block.UPDATE_CLIENTS);
			}
		}
		return true;
	}

	// Air or plants like short grass
	private static boolean isFree(WorldGenLevel level, BlockPos pos) {
		return level.getBlockState(pos).canBeReplaced() && level.getFluidState(pos).isEmpty();
	}
}
