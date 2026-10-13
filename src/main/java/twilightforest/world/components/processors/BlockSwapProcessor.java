package twilightforest.world.components.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructureProcessors;
import twilightforest.util.features.FeaturePlacers;

import java.util.Map;

/**
 * Replaces blocks one to one while keeping every block state property both blocks share (stair facing, slab type, wall connections...),
 * which the vanilla rule processor can't do. Used to re-skin imported structures.
 */
public class BlockSwapProcessor extends StructureProcessor {
	public static final MapCodec<BlockSwapProcessor> CODEC = Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), BuiltInRegistries.BLOCK.byNameCodec())
		.fieldOf("swaps").xmap(BlockSwapProcessor::new, processor -> processor.swaps);

	private final Map<Block, Block> swaps;

	public BlockSwapProcessor(Map<Block, Block> swaps) {
		this.swaps = swaps;
	}

	@Override
	public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
		BlockState state = relativeBlockInfo.state();
		Block replacement = this.swaps.get(state.getBlock());
		if (replacement == null)
			return relativeBlockInfo;

		return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), FeaturePlacers.transferAllStateKeys(state, replacement), relativeBlockInfo.nbt());
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TFStructureProcessors.BLOCK_SWAP.get();
	}
}
