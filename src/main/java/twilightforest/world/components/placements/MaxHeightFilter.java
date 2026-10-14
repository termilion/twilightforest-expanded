package twilightforest.world.components.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import twilightforest.init.TFFeatureModifiers;

/**
 * Drops positions above an absolute Y level, e.g. after a heightmap placement to keep features off high terrain
 */
public class MaxHeightFilter extends PlacementFilter {
	public static final MapCodec<MaxHeightFilter> CODEC = Codec.INT.fieldOf("max_y").xmap(MaxHeightFilter::new, filter -> filter.maxY);

	private final int maxY;

	public MaxHeightFilter(int maxY) {
		this.maxY = maxY;
	}

	@Override
	protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
		return pos.getY() <= this.maxY;
	}

	@Override
	public PlacementModifierType<?> type() {
		return TFFeatureModifiers.MAX_HEIGHT.get();
	}
}
