package twilightforest.world.components.structures.type;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.biomesources.TFBiomeProvider;

import java.util.Optional;

/**
 * A plain jigsaw structure (same fields as minecraft:jigsaw) that only generates in the Twilight Forest, so it can list vanilla biomes
 * the Twilight Forest borrows (e.g. minecraft:mangrove_swamp) without appearing in the Overworld.
 * Generation is delegated to a vanilla {@link JigsawStructure} (final, so it can't be extended).
 */
public class TwilightJigsawStructure extends Structure {
	public static final MapCodec<TwilightJigsawStructure> CODEC = JigsawStructure.CODEC.xmap(TwilightJigsawStructure::new, s -> s.jigsaw);

	private final JigsawStructure jigsaw;

	public TwilightJigsawStructure(JigsawStructure jigsaw) {
		super(settingsOf(jigsaw));
		this.jigsaw = jigsaw;
	}

	// The settings a wrapper around a vanilla jigsaw structure shares with it
	static StructureSettings settingsOf(JigsawStructure jigsaw) {
		return jigsaw.modifiableStructureInfo().getOriginalStructureInfo().structureSettings();
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
