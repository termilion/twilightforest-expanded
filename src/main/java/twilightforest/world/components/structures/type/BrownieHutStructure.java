package twilightforest.world.components.structures.type;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.biomesources.TFBiomeProvider;
import twilightforest.world.components.structures.browniehut.BrownieHutPiece;

import java.util.Map;
import java.util.Optional;

// The vanilla swamp hut with a brownie instead of the witch, Twilight Forest only (its biomes include minecraft:mangrove_swamp)
public class BrownieHutStructure extends Structure {
	public static final MapCodec<BrownieHutStructure> CODEC = simpleCodec(BrownieHutStructure::new);

	public BrownieHutStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	public Optional<GenerationStub> findValidGenerationPoint(GenerationContext context) {
		if (!(context.biomeSource() instanceof TFBiomeProvider))
			return Optional.empty();
		return super.findValidGenerationPoint(context);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> builder.addPiece(new BrownieHutPiece(context.random(), context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ())));
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.BROWNIE_HUT.get();
	}

	public static BrownieHutStructure buildStructureConfig(HolderSet<Biome> biomes) {
		return new BrownieHutStructure(new StructureSettings(biomes, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE));
	}
}
