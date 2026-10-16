package twilightforest.world.components.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import twilightforest.init.TFStructureProcessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * After a template is processed, places plants next to some of its blocks: on top (mushrooms on mycelium) or hanging below (roots under stone).
 * A spot is only used when neither the template nor the world has a block there. Put it last in a processor list so it sees the final states.
 */
public class AttachPlantsProcessor extends StructureProcessor {
	public static final MapCodec<AttachPlantsProcessor> CODEC = Rule.CODEC.listOf().fieldOf("rules").xmap(AttachPlantsProcessor::new, processor -> processor.rules);

	private final List<Rule> rules;

	public AttachPlantsProcessor(List<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos offset, BlockPos pos, List<StructureTemplate.StructureBlockInfo> originalBlockInfos, List<StructureTemplate.StructureBlockInfo> processedBlockInfos, StructurePlaceSettings settings) {
		BoundingBox bounds = settings.getBoundingBox();
		// Only spots inside the bounds are ever looked up
		Map<BlockPos, BlockState> template = new HashMap<>();
		for (StructureTemplate.StructureBlockInfo info : processedBlockInfos)
			if (bounds == null || bounds.isInside(info.pos()))
				template.put(info.pos(), info.state());

		List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>(processedBlockInfos);
		for (StructureTemplate.StructureBlockInfo info : processedBlockInfos) {
			// Per-position random, shared by the rules so each rule rolls independently
			RandomSource random = null;
			for (Rule rule : this.rules) {
				if (!info.state().is(rule.targets()))
					continue;

				if (random == null)
					random = settings.getRandom(info.pos());
				if (random.nextFloat() >= rule.chance())
					continue;

				int length = 1 + random.nextInt(rule.maxLength());
				BlockState plant = rule.plants().getRandomValue(random).orElse(null);
				if (plant == null)
					continue;

				BlockPos.MutableBlockPos spot = info.pos().mutable();
				for (int i = 0; i < length; i++) {
					spot.move(rule.direction());
					// Reading the world outside the chunk being generated is not allowed
					if (bounds != null && !bounds.isInside(spot))
						break;
					BlockState inTemplate = template.get(spot);
					if ((inTemplate != null && !inTemplate.isAir()) || !level.getBlockState(spot).isAir())
						break;

					BlockPos placed = spot.immutable();
					template.put(placed, plant);
					result.add(new StructureTemplate.StructureBlockInfo(placed, plant, null));
				}
			}
		}
		return result;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TFStructureProcessors.ATTACH_PLANTS.get();
	}

	/**
	 * @param targets   blocks the plants attach to
	 * @param direction side of the target the plants go to (up = on top, down = hanging)
	 * @param chance    chance per target block
	 * @param plants    weighted plant states
	 * @param maxLength plants stack 1..maxLength blocks (e.g. hanging root strands)
	 */
	public record Rule(HolderSet<Block> targets, Direction direction, float chance, SimpleWeightedRandomList<BlockState> plants, int maxLength) {
		public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("targets").forGetter(Rule::targets),
			Direction.CODEC.fieldOf("direction").forGetter(Rule::direction),
			Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(Rule::chance),
			SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC).fieldOf("plants").forGetter(Rule::plants),
			Codec.intRange(1, 16).optionalFieldOf("max_length", 1).forGetter(Rule::maxLength)
		).apply(instance, Rule::new));
	}
}
