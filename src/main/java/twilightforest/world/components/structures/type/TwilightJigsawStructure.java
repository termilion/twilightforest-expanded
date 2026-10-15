package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.biomesources.TFBiomeProvider;

import java.util.List;
import java.util.Optional;

/**
 * A plain jigsaw structure (same fields as minecraft:jigsaw) that only generates in the Twilight Forest, so it can list vanilla biomes
 * the Twilight Forest borrows (e.g. minecraft:mangrove_swamp) without appearing in the Overworld.
 * Generation is delegated to a vanilla {@link JigsawStructure} (final, so it can't be extended).
 */
public class TwilightJigsawStructure extends Structure {
	public static final MapCodec<TwilightJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		settingsCodec(instance),
		StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
		ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(s -> s.startJigsawName),
		Codec.intRange(0, JigsawStructure.MAX_DEPTH).fieldOf("size").forGetter(s -> s.maxDepth),
		HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
		Codec.BOOL.optionalFieldOf("use_expansion_hack", false).forGetter(s -> s.useExpansionHack),
		Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
		Codec.intRange(1, JigsawStructure.MAX_TOTAL_STRUCTURE_RANGE).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
		Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(s -> s.poolAliases),
		DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING).forGetter(s -> s.dimensionPadding),
		LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS).forGetter(s -> s.liquidSettings)
	).apply(instance, TwilightJigsawStructure::new));

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
	private final JigsawStructure jigsaw;

	public TwilightJigsawStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, List<PoolAliasBinding> poolAliases, DimensionPadding dimensionPadding, LiquidSettings liquidSettings) {
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
	}

	@Override
	public Optional<GenerationStub> findValidGenerationPoint(GenerationContext context) {
		if (!(context.biomeSource() instanceof TFBiomeProvider))
			return Optional.empty();
		return super.findValidGenerationPoint(context);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		return this.jigsaw.findGenerationPoint(context);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.TWILIGHT_JIGSAW.get();
	}
}
