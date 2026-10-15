package twilightforest.world.components.structures.type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.biomesources.TFBiomeProvider;

import java.util.List;
import java.util.Optional;

/**
 * A vanilla ruined portal that never generates in the Twilight Forest. Used to override {@code minecraft:ruined_portal_swamp},
 * whose biome tag contains the mangrove swamp shared by the Overworld and the Twilight Forest.
 */
public class OverworldRuinedPortalStructure extends RuinedPortalStructure {
	public static final MapCodec<OverworldRuinedPortalStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		settingsCodec(instance),
		ExtraCodecs.nonEmptyList(RuinedPortalStructure.Setup.CODEC.listOf()).fieldOf("setups").forGetter(s -> s.setups)
	).apply(instance, OverworldRuinedPortalStructure::new));

	private final List<RuinedPortalStructure.Setup> setups;

	public OverworldRuinedPortalStructure(Structure.StructureSettings settings, List<RuinedPortalStructure.Setup> setups) {
		super(settings, setups);
		this.setups = setups;
	}

	@Override
	public Optional<GenerationStub> findValidGenerationPoint(GenerationContext context) {
		return context.biomeSource() instanceof TFBiomeProvider ? Optional.empty() : super.findValidGenerationPoint(context);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.OVERWORLD_RUINED_PORTAL.get();
	}
}
