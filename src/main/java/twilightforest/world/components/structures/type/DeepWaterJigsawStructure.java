package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import twilightforest.init.TFStructureTypes;

import java.util.List;
import java.util.Optional;

/**
 * A plain jigsaw structure placed on the ocean floor (start height is relative to OCEAN_FLOOR_WG), used for datapack-style
 * underwater structures imported from other projects. It only starts where the floor at the chunk corner and center lies at or below
 * {@code max_floor_y}, so taller ruins stay in the deep parts of lakes instead of sticking out near the shore.
 * {@code min_start_y} lifts the start piece over deep floors, so pieces hanging below it fit in. No piece ever reaches the
 * bottom (bedrock) layer; the template pools additionally use a protected_blocks processor so bedrock is never replaced.
 */
public class DeepWaterJigsawStructure extends Structure {
	private static final DimensionPadding BEDROCK_PADDING = new DimensionPadding(1, 0);

	public static final MapCodec<DeepWaterJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		settingsCodec(instance),
		StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
		Codec.intRange(0, JigsawStructure.MAX_DEPTH).fieldOf("size").forGetter(s -> s.maxDepth),
		HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
		Codec.intRange(1, JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
		Codec.INT.fieldOf("max_floor_y").forGetter(s -> s.maxFloorY),
		Codec.INT.fieldOf("min_start_y").forGetter(s -> s.minStartY),
		LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(s -> s.liquidSettings)
	).apply(instance, DeepWaterJigsawStructure::new));

	private final Holder<StructureTemplatePool> startPool;
	private final int maxDepth;
	private final HeightProvider startHeight;
	private final int maxDistanceFromCenter;
	private final int maxFloorY;
	private final int minStartY;
	private final LiquidSettings liquidSettings;

	public DeepWaterJigsawStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, HeightProvider startHeight, int maxDistanceFromCenter, int maxFloorY, int minStartY, LiquidSettings liquidSettings) {
		super(settings);
		this.startPool = startPool;
		this.maxDepth = maxDepth;
		this.startHeight = startHeight;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
		this.maxFloorY = maxFloorY;
		this.minStartY = minStartY;
		this.liquidSettings = liquidSettings;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		int cornerFloor = this.floorY(context, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
		if (cornerFloor > this.maxFloorY || this.floorY(context, chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ()) > this.maxFloorY)
			return Optional.empty();

		// The start is projected onto the floor below the start piece's center, which lies somewhere within 12 blocks of the chunk corner
		// (depending on rotation), and sits one block below it (ground level delta). Offset from the lowest floor around it so the
		// start piece never reaches below min_start_y
		int lowestFloor = cornerFloor;
		for (int dx = -12; dx <= 12; dx += 12)
			for (int dz = -12; dz <= 12; dz += 12)
				if (dx != 0 || dz != 0)
					lowestFloor = Math.min(lowestFloor, this.floorY(context, chunkPos.getMinBlockX() + dx, chunkPos.getMinBlockZ() + dz));
		int y = Math.max(this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor())), this.minStartY + 1 - lowestFloor);
		BlockPos startPos = new BlockPos(chunkPos.getMinBlockX(), y, chunkPos.getMinBlockZ());

		// The bottom padding keeps every child piece off the bedrock floor
		return JigsawPlacement.addPieces(context, this.startPool, Optional.empty(), this.maxDepth, startPos, false, Optional.of(Heightmap.Types.OCEAN_FLOOR_WG), this.maxDistanceFromCenter, PoolAliasLookup.EMPTY, BEDROCK_PADDING, this.liquidSettings)
			.map(stub -> new GenerationStub(stub.position(), builder -> this.liftAboveBedrock(context, stub, builder)));
	}

	// Last resort for the start piece: if anything still reaches the bottom layer, move the whole structure up
	private void liftAboveBedrock(GenerationContext context, GenerationStub stub, StructurePiecesBuilder builder) {
		List<StructurePiece> pieces = stub.getPiecesBuilder().build().pieces();
		int lowest = pieces.stream().mapToInt(piece -> piece.getBoundingBox().minY()).min().orElse(Integer.MAX_VALUE);
		int lift = context.heightAccessor().getMinBuildHeight() + BEDROCK_PADDING.bottom() - lowest;
		for (StructurePiece piece : pieces) {
			if (lift > 0)
				piece.move(0, lift, 0);
			builder.addPiece(piece);
		}
	}

	private int floorY(GenerationContext context, int x, int z) {
		return context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.DEEP_WATER_JIGSAW.get();
	}
}
