package twilightforest.world.components.structures.type;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructureTypes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * A plain jigsaw structure that refuses to generate where its pieces would overlap any other structure.
 * Every structure set of the dimension is checked: each nearby chunk its placement picks is generated in a scratch context (same seed and
 * random as the real generation, so the layout is identical) and the bounding boxes are compared. Exclusive structures among themselves:
 * the one with the lower start chunk (x, then z) wins, unless it is blocked itself.
 * Generation itself is delegated to a vanilla {@link JigsawStructure} (final, so it can't be extended).
 */
public class ExclusiveJigsawStructure extends Structure {
	public static final MapCodec<ExclusiveJigsawStructure> CODEC = RecordCodecBuilder.<ExclusiveJigsawStructure>mapCodec(instance -> instance.group(
		settingsCodec(instance),
		StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
		ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
		Codec.intRange(0, JigsawStructure.MAX_DEPTH).fieldOf("size").forGetter(s -> s.maxDepth),
		HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
		Codec.BOOL.fieldOf("use_expansion_hack").forGetter(s -> s.useExpansionHack),
		Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
		Codec.intRange(1, JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
		Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(s -> s.poolAliases),
		DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(s -> s.dimensionPadding),
		LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(s -> s.liquidSettings),
		Codec.intRange(0, 32).optionalFieldOf("search_radius_chunks", 8).forGetter(s -> s.searchRadiusChunks),
		Codec.intRange(0, 64).optionalFieldOf("margin", 2).forGetter(s -> s.margin),
		ResourceLocation.CODEC.listOf().optionalFieldOf("ignored_structure_sets", List.of()).forGetter(s -> s.ignoredStructureSets)
	).apply(instance, ExclusiveJigsawStructure::new)).validate(ExclusiveJigsawStructure::verifyRange);

	// One placement state per dimension (RandomState is unique per dimension and server)
	private static final Map<RandomState, ChunkGeneratorStructureState> STATES = Collections.synchronizedMap(new WeakHashMap<>());
	private static final int BOX_CACHE_SIZE = 4096;
	private static final Map<BoxKey, Optional<BoundingBox>> BOXES = Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75F, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<BoxKey, Optional<BoundingBox>> eldest) {
			return this.size() > BOX_CACHE_SIZE;
		}
	});

	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int maxDepth;
	private final HeightProvider startHeight;
	private final boolean useExpansionHack;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;
	private final List<PoolAliasBinding> poolAliases;
	private final DimensionPadding dimensionPadding;
	private final LiquidSettings liquidSettings;
	// How far (in chunks) beyond this structure's own box another structure's start chunk is looked for
	private final int searchRadiusChunks;
	// Extra blocks kept free around this structure's pieces
	private final int margin;
	// Structure sets this structure may overlap (e.g. small ruins that would otherwise block a large town almost everywhere)
	private final List<ResourceLocation> ignoredStructureSets;
	private final JigsawStructure jigsaw;

	public ExclusiveJigsawStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, List<PoolAliasBinding> poolAliases, DimensionPadding dimensionPadding, LiquidSettings liquidSettings, int searchRadiusChunks, int margin, List<ResourceLocation> ignoredStructureSets) {
		super(settings);
		this.jigsaw = new JigsawStructure(settings, startPool, startJigsawName, maxDepth, startHeight, useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter, poolAliases, dimensionPadding, liquidSettings);
		this.startPool = startPool;
		this.startJigsawName = startJigsawName;
		this.maxDepth = maxDepth;
		this.startHeight = startHeight;
		this.useExpansionHack = useExpansionHack;
		this.projectStartToHeightmap = projectStartToHeightmap;
		this.maxDistanceFromCenter = maxDistanceFromCenter;
		this.poolAliases = poolAliases;
		this.dimensionPadding = dimensionPadding;
		this.liquidSettings = liquidSettings;
		this.searchRadiusChunks = searchRadiusChunks;
		this.margin = margin;
		this.ignoredStructureSets = ignoredStructureSets;
	}

	// Same limit as JigsawStructure#verifyRange
	private static DataResult<ExclusiveJigsawStructure> verifyRange(ExclusiveJigsawStructure structure) {
		int padding = switch (structure.terrainAdaptation()) {
			case NONE -> 0;
			case BURY, BEARD_THIN, BEARD_BOX, ENCAPSULATE -> 12;
		};
		return structure.maxDistanceFromCenter + padding > JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE
			? DataResult.error(() -> "Structure size including terrain adaptation must not exceed " + JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE)
			: DataResult.success(structure);
	}

	@Override
	public Optional<GenerationStub> findValidGenerationPoint(GenerationContext context) {
		Optional<GenerationStub> stub = super.findValidGenerationPoint(context);
		if (stub.isEmpty())
			return stub;

		// Build the pieces once, keep them in the returned stub so the real generation doesn't assemble them again
		StructurePiecesBuilder pieces = stub.get().getPiecesBuilder();
		if (pieces.isEmpty())
			return Optional.empty();

		BoundingBox box = pieces.getBoundingBox().inflatedBy(this.margin);
		if (this.overlapsOtherStructure(context, box))
			return Optional.empty();

		return Optional.of(new GenerationStub(stub.get().position(), Either.right(pieces)));
	}

	private boolean overlapsOtherStructure(GenerationContext context, BoundingBox box) {
		ChunkGeneratorStructureState state = STATES.computeIfAbsent(context.randomState(), randomState ->
			ChunkGeneratorStructureState.createForNormal(randomState, context.seed(), context.biomeSource(), context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)));
		ChunkPos own = context.chunkPos();

		int minChunkX = (box.minX() >> 4) - this.searchRadiusChunks;
		int maxChunkX = (box.maxX() >> 4) + this.searchRadiusChunks;
		int minChunkZ = (box.minZ() >> 4) - this.searchRadiusChunks;
		int maxChunkZ = (box.maxZ() >> 4) + this.searchRadiusChunks;

		for (Holder<StructureSet> set : state.possibleStructureSets()) {
			if (set.unwrapKey().map(key -> this.ignoredStructureSets.contains(key.location())).orElse(false))
				continue;
			StructurePlacement placement = set.value().placement();
			for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
				for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
					if (chunkX == own.x && chunkZ == own.z)
						continue;
					if (!placement.isStructureChunk(state, chunkX, chunkZ))
						continue;

					// Sets with several structures generate only one of them; checking all of them is the safe side
					for (StructureSet.StructureSelectionEntry entry : set.value().structures()) {
						Optional<BoundingBox> other = this.otherStructureBox(context, entry.structure().value(), new ChunkPos(chunkX, chunkZ));
						if (other.isPresent() && other.get().intersects(box))
							return true;
					}
				}
			}
		}
		return false;
	}

	private Optional<BoundingBox> otherStructureBox(GenerationContext context, Structure structure, ChunkPos chunk) {
		if (structure instanceof ExclusiveJigsawStructure) {
			// Lower start chunk wins, so a chain of exclusive structures always ends
			if (chunk.x > context.chunkPos().x || (chunk.x == context.chunkPos().x && chunk.z > context.chunkPos().z))
				return Optional.empty();
		}

		// Large neighbours (e.g. the lone citadel) are checked by many candidates, so their boxes are cached
		BoxKey key = new BoxKey(context.randomState(), structure, chunk.toLong());
		Optional<BoundingBox> cached = BOXES.get(key);
		if (cached != null)
			return cached;

		// Same context as ChunkGenerator#tryGenerateStructure builds, so the scratch layout matches the real one
		GenerationContext otherContext = new GenerationContext(context.registryAccess(), context.chunkGenerator(), context.biomeSource(), context.randomState(),
			context.structureTemplateManager(), context.seed(), chunk, context.heightAccessor(), structure.biomes()::contains);

		Optional<BoundingBox> box;
		try {
			// For exclusive structures this runs their own overlap check too, so one that is blocked itself blocks nothing
			Optional<GenerationStub> stub = structure.findValidGenerationPoint(otherContext);
			StructurePiecesBuilder pieces = stub.map(GenerationStub::getPiecesBuilder).orElse(null);
			box = pieces == null || pieces.isEmpty() ? Optional.empty() : Optional.of(pieces.getBoundingBox());
		} catch (RuntimeException e) {
			TwilightForestMod.LOGGER.warn("Could not check structure {} at chunk {} for overlaps", structure, chunk, e);
			box = Optional.empty();
		}
		BOXES.put(key, box);
		return box;
	}

	private record BoxKey(RandomState randomState, Structure structure, long chunk) {
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		return this.jigsaw.findGenerationPoint(context);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.EXCLUSIVE_JIGSAW.get();
	}
}
