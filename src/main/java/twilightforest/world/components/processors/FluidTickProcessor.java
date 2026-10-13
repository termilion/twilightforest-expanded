package twilightforest.world.components.processors;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructureProcessors;

/**
 * Schedules a fluid tick for every fluid a template places, so the water starts flowing right away
 * instead of waiting for a neighbouring block update (templates never tick their fluids).
 * Must come after processors that turn blocks into fluids.
 */
public class FluidTickProcessor extends StructureProcessor {
	public static final FluidTickProcessor INSTANCE = new FluidTickProcessor();
	public static final MapCodec<FluidTickProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

	private FluidTickProcessor() {
	}

	@Override
	public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
		FluidState fluid = relativeBlockInfo.state().getFluidState();
		// During worldgen this is the WorldGenRegion, the tick lands in the proto chunk like the ones of vanilla springs.
		// Templates are processed as a whole, but only the part inside the bounding box (the current chunk) is placed and writable
		BoundingBox box = settings.getBoundingBox();
		if (!fluid.isEmpty() && (box == null || box.isInside(relativeBlockInfo.pos())) && level instanceof LevelAccessor accessor)
			accessor.scheduleTick(relativeBlockInfo.pos(), fluid.getType(), fluid.getType().getTickDelay(level));

		return relativeBlockInfo;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TFStructureProcessors.FLUID_TICK.get();
	}
}
