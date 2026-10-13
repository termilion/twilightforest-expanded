package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.SpiralStairShaftPiece;
import twilightforest.world.components.biomesources.TFBiomeProvider;
import twilightforest.world.components.structures.util.DecorationClearance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A plain jigsaw structure for the landmark grid, used for datapack-style structures imported from other projects.
 * Unlike {@link JigsawStructure}, the biome is checked against the main biome of the landmark tile instead of the biome at the start position,
 * so the structure can start underground (where the biome is twilightforest:underground) and still be bound to the surface biome.
 * An optional entrance places a second jigsaw structure on the surface above the start piece and connects both with a spiral staircase.
 */
public class LandmarkJigsawStructure extends Structure implements DecorationClearance {
	public static final MapCodec<LandmarkJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		settingsCodec(instance),
		StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
		ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
		Codec.intRange(0, JigsawStructure.MAX_DEPTH).fieldOf("size").forGetter(s -> s.maxDepth),
		HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
		Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
		Codec.intRange(1, JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
		DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(s -> s.dimensionPadding),
		LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(s -> s.liquidSettings),
		Entrance.CODEC.optionalFieldOf("entrance").forGetter(s -> s.entrance),
		DecorationConfig.CODEC.optionalFieldOf(DecorationClearance.CODEC_NAME).forGetter(s -> s.decorationConfig)
	).apply(instance, LandmarkJigsawStructure::new));

	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int maxDepth;
	private final HeightProvider startHeight;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;
	private final DimensionPadding dimensionPadding;
	private final LiquidSettings liquidSettings;
	private final Optional<Entrance> entrance;
	// Without a config, biome features may generate anywhere around and inside the structure
	private final Optional<DecorationConfig> decorationConfig;

	public LandmarkJigsawStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, DimensionPadding dimensionPadding, LiquidSettings liquidSettings, Optional<Entrance> entrance, Optional<DecorationConfig> decorationConfig) {
		super(settings);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.maxDepth = maxDepth;
		this.startHeight = startHeight;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
		this.dimensionPadding = dimensionPadding;
		this.liquidSettings = liquidSettings;
		this.entrance = entrance;
		this.decorationConfig = decorationConfig;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		int y = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
		BlockPos startPos = new BlockPos(chunkPos.getMinBlockX(), y, chunkPos.getMinBlockZ());

		Optional<GenerationStub> stub = JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.maxDepth, startPos, false, this.projectStartToHeightmap, this.maxDistanceFromCenter, PoolAliasLookup.create(List.of(), startPos, context.seed()), this.dimensionPadding, this.liquidSettings);

		if (this.entrance.isEmpty())
			return stub;

		Entrance entrance = this.entrance.get();
		return stub.map(mainStub -> new GenerationStub(mainStub.position(), builder -> this.addPiecesWithEntrance(context, entrance, mainStub, builder)));
	}

	private void addPiecesWithEntrance(GenerationContext context, Entrance entrance, GenerationStub mainStub, StructurePiecesBuilder builder) {
		List<StructurePiece> mainPieces = mainStub.getPiecesBuilder().build().pieces();
		mainPieces.forEach(builder::addPiece);
		if (mainPieces.isEmpty())
			return;

		// The entrance building stands on the surface above the center of the start piece
		BoundingBox startRoom = mainPieces.getFirst().getBoundingBox();
		BlockPos aboveStart = new BlockPos(startRoom.getCenter().getX(), 0, startRoom.getCenter().getZ());
		Optional<GenerationStub> entranceStub = JigsawPlacement.addPieces(context, entrance.startPool(), Optional.of(entrance.startJigsawName()), entrance.size(), aboveStart, false, Optional.of(Heightmap.Types.WORLD_SURFACE_WG), JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE, PoolAliasLookup.EMPTY, DimensionPadding.ZERO, this.liquidSettings);
		if (entranceStub.isEmpty())
			return;

		List<StructurePiece> entrancePieces = entranceStub.get().getPiecesBuilder().build().pieces();
		entrancePieces.forEach(builder::addPiece);
		if (!(entrancePieces.getFirst() instanceof PoolElementStructurePiece building))
			return;

		BlockPos shaftTop = StructureTemplate.transform(entrance.shaftPosition(), Mirror.NONE, building.getRotation(), BlockPos.ZERO).offset(building.getPosition());

		// Wherever the shaft passes through pieces of the main structure, it must not wall them off
		List<int[]> openRanges = new ArrayList<>();
		BoundingBox shaftColumn = new BoundingBox(shaftTop.getX() - 2, startRoom.minY(), shaftTop.getZ() - 2, shaftTop.getX() + 2, shaftTop.getY(), shaftTop.getZ() + 2);
		for (StructurePiece piece : mainPieces)
			if (piece.getBoundingBox().intersects(shaftColumn))
				openRanges.add(new int[]{piece.getBoundingBox().minY(), piece.getBoundingBox().maxY()});

		// Added last, so it carves through everything placed before it
		builder.addPiece(new SpiralStairShaftPiece(shaftTop.getX(), shaftTop.getZ(), shaftTop.getY(), startRoom.minY() + 1, building.getBoundingBox().minY() - 1, openRanges));
	}

	@Override
	public Optional<GenerationStub> findValidGenerationPoint(GenerationContext context) {
		// Landmarks only belong in the Twilight Forest, see LandmarkStructure#findValidGenerationPoint
		if (!(context.biomeSource() instanceof TFBiomeProvider twilightBiomeProvider))
			return Optional.empty();

		// Same biome-grid tile center as LandmarkStructure
		ChunkPos chunkPos = context.chunkPos();
		int biomeX = (Math.round(chunkPos.x / 16F) << 6) + 2;
		int biomeZ = (Math.round(chunkPos.z / 16F) << 6) + 2;
		Holder<Biome> biomeAt = twilightBiomeProvider.getMainBiome(biomeX, biomeZ);

		return context.validBiome().test(biomeAt) ? this.findGenerationPoint(context) : Optional.empty();
	}

	/**
	 * @param startPool       pool of the surface building, placed on the surface above the start piece of the main structure
	 * @param startJigsawName jigsaw block of the building that is aligned with the center of the start piece
	 * @param shaftPosition   position of the top stair step inside the (unrotated) building template; the spiral staircase descends from there
	 * @param size            jigsaw depth of the building, e.g. for foundation pieces
	 */
	public record Entrance(Holder<StructureTemplatePool> startPool, ResourceLocation startJigsawName, BlockPos shaftPosition, int size) {
		public static final Codec<Entrance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(Entrance::startPool),
			ResourceLocation.CODEC.fieldOf("start_jigsaw_name").forGetter(Entrance::startJigsawName),
			BlockPos.CODEC.fieldOf("shaft_position").forGetter(Entrance::shaftPosition),
			Codec.intRange(0, JigsawStructure.MAX_DEPTH).optionalFieldOf("size", 1).forGetter(Entrance::size)
		).apply(instance, Entrance::new));
	}

	@Override
	public float chunkClearanceRadius() {
		return this.decorationConfig.map(DecorationConfig::chunkClearanceRadius).orElse(0F);
	}

	@Override
	public boolean isSurfaceDecorationsAllowed() {
		return this.decorationConfig.map(DecorationConfig::surfaceDecorations).orElse(true);
	}

	@Override
	public boolean isUndergroundDecoAllowed() {
		return this.decorationConfig.map(DecorationConfig::undergroundDecorations).orElse(true);
	}

	@Override
	public boolean isGrassDecoAllowed() {
		return this.decorationConfig.map(DecorationConfig::vegetation).orElse(true);
	}

	@Override
	public boolean shouldAdjustToTerrain() {
		return false;
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.LANDMARK_JIGSAW.get();
	}
}
