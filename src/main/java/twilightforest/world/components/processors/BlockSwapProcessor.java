package twilightforest.world.components.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Replaces blocks one to one while keeping every block state property both blocks share (stair facing, slab type, wall connections...),
 * which the vanilla rule processor can't do. Used to re-skin imported structures.
 * {@code woods} swaps whole wood sets by name (e.g. every {@code minecraft:oak_*} shape to the same {@code twilightforest:dark_*} shape),
 * {@code swaps} lists single blocks and wins over the wood sets.
 */
public class BlockSwapProcessor extends StructureProcessor {
	public static final MapCodec<BlockSwapProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		WoodSwap.CODEC.listOf().optionalFieldOf("woods", List.of()).forGetter(processor -> processor.woods),
		Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), BuiltInRegistries.BLOCK.byNameCodec()).optionalFieldOf("swaps", Map.of()).forGetter(processor -> processor.swaps)
	).apply(instance, BlockSwapProcessor::new));

	private final List<WoodSwap> woods;
	private final Map<Block, Block> swaps;
	private final Map<Block, Block> resolved = new HashMap<>();

	public BlockSwapProcessor(List<WoodSwap> woods, Map<Block, Block> swaps) {
		this.woods = woods;
		this.swaps = swaps;
		woods.forEach(wood -> wood.addSwaps(this.resolved));
		this.resolved.putAll(swaps);
	}

	@Override
	public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
		BlockState state = relativeBlockInfo.state();
		Block replacement = this.resolved.get(state.getBlock());
		if (replacement == null)
			return relativeBlockInfo;

		return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), FeaturePlacers.transferAllStateKeys(state, replacement), relativeBlockInfo.nbt());
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TFStructureProcessors.BLOCK_SWAP.get();
	}

	/**
	 * Swaps every shape of the {@code from} woods to the same shape of the {@code to} wood, where both blocks exist.
	 * A wood is a namespace plus name prefix ({@code minecraft:oak} → {@code minecraft:oak_planks}, {@code minecraft:stripped_oak_log}, ...).
	 *
	 * @param skipShapes shapes left unchanged, e.g. {@code leaves}
	 */
	public record WoodSwap(List<ResourceLocation> from, ResourceLocation to, List<String> skipShapes) {
		public static final Codec<WoodSwap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.listOf().fieldOf("from").forGetter(WoodSwap::from),
			ResourceLocation.CODEC.fieldOf("to").forGetter(WoodSwap::to),
			Codec.STRING.listOf().optionalFieldOf("skip_shapes", List.of()).forGetter(WoodSwap::skipShapes)
		).apply(instance, WoodSwap::new));

		private static final List<String> SHAPES = List.of(
			"log", "wood", "stripped_log", "stripped_wood", "planks", "stairs", "slab", "fence", "fence_gate", "door", "trapdoor",
			"button", "pressure_plate", "sign", "wall_sign", "hanging_sign", "wall_hanging_sign", "leaves", "sapling");

		private void addSwaps(Map<Block, Block> swaps) {
			for (String shape : SHAPES) {
				if (this.skipShapes.contains(shape))
					continue;
				Optional<Block> target = block(this.to, shape);
				if (target.isEmpty())
					continue;
				for (ResourceLocation wood : this.from)
					block(wood, shape).ifPresent(source -> swaps.put(source, target.get()));
			}
		}

		private static Optional<Block> block(ResourceLocation wood, String shape) {
			String name = shape.startsWith("stripped_") ? "stripped_" + wood.getPath() + "_" + shape.substring("stripped_".length()) : wood.getPath() + "_" + shape;
			return BuiltInRegistries.BLOCK.getOptional(wood.withPath(name));
		}
	}
}
