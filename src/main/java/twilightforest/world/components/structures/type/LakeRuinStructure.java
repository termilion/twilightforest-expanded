package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.lakeruin.LakeRuinPiece;

import java.util.Map;
import java.util.Optional;

/**
 * Underwater ruins in the deep parts of lakes. Uses the vanilla cold ocean ruin templates (brick base with cracked and mossy overlays).
 */
public class LakeRuinStructure extends Structure {
	public static final MapCodec<LakeRuinStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Structure.settingsCodec(instance),
		Codec.floatRange(0, 1).fieldOf("large_probability").forGetter(s -> s.largeProbability),
		Codec.INT.fieldOf("max_floor_y").forGetter(s -> s.maxFloorY)
	).apply(instance, LakeRuinStructure::new));

	private static final int[] SMALL = {1, 2, 3, 4, 5, 6, 7, 8};
	private static final int[] BIG = {1, 2, 3, 8};

	private final float largeProbability;
	private final int maxFloorY;

	public LakeRuinStructure(StructureSettings settings, float largeProbability, int maxFloorY) {
		super(settings);
		this.largeProbability = largeProbability;
		this.maxFloorY = maxFloorY;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		BlockPos center = context.chunkPos().getMiddleBlockPosition(0);
		int floorY = context.chunkGenerator().getBaseHeight(center.getX(), center.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
		// Deep water only
		if (floorY > this.maxFloorY)
			return Optional.empty();

		BlockPos pos = new BlockPos(center.getX(), floorY, center.getZ());
		return Optional.of(new GenerationStub(pos, builder -> this.generatePieces(builder, context, context.chunkPos().getWorldPosition())));
	}

	private void generatePieces(StructurePiecesBuilder builder, GenerationContext context, BlockPos pos) {
		RandomSource random = context.random();
		Rotation rotation = Rotation.getRandom(random);
		boolean large = random.nextFloat() <= this.largeProbability;
		int variant = large ? BIG[random.nextInt(BIG.length)] : SMALL[random.nextInt(SMALL.length)];
		String prefix = large ? "underwater_ruin/big_" : "underwater_ruin/";

		builder.addPiece(new LakeRuinPiece(context.structureTemplateManager(), ResourceLocation.withDefaultNamespace(prefix + "brick_" + variant), pos, rotation, large ? 0.9F : 0.8F));
		builder.addPiece(new LakeRuinPiece(context.structureTemplateManager(), ResourceLocation.withDefaultNamespace(prefix + "cracked_" + variant), pos, rotation, 0.7F));
		builder.addPiece(new LakeRuinPiece(context.structureTemplateManager(), ResourceLocation.withDefaultNamespace(prefix + "mossy_" + variant), pos, rotation, 0.5F));
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.LAKE_RUIN.get();
	}

	public static LakeRuinStructure buildStructureConfig(HolderSet<Biome> biomes) {
		return new LakeRuinStructure(
			new StructureSettings(biomes, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE),
			0.3F, -14
		);
	}
}
