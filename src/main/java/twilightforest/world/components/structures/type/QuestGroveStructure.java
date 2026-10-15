package twilightforest.world.components.structures.type;

import twilightforest.world.components.structures.util.GroundFill;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import twilightforest.data.tags.BiomeTagGenerator;
import twilightforest.init.TFMapDecorations;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.QuestGrove;
import twilightforest.world.components.structures.util.ConquerableStructure;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestGroveStructure extends ConquerableStructure {
	public static final MapCodec<QuestGroveStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> landmarkCodec(instance).apply(instance, QuestGroveStructure::new));
	public static final int LENGTH = 27;

	public QuestGroveStructure(Optional<DecorationConfig> decorationConfig, boolean centerInChunk, Optional<Holder<MapDecorationType>> structureIcon, StructureSettings structureSettings) {
		super(decorationConfig, centerInChunk, structureIcon, structureSettings);
	}

	@Override
	protected StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
		return new QuestGrove(context.structureTemplateManager(), new BlockPos(x - LENGTH / 2 + 1, y + 2, z - LENGTH / 2 + 1)); // + 1 offsets to center the structure
	}

	// The grove sits on raised lake islands, the default clamp (sea level + 1..7) would bury it
	@Override
	public int adjustForTerrain(GenerationContext context, int x, int z) {
		return super.adjustForTerrain(context, x, z) + 12;
	}

	// Fill dirt between the structure and the ground below it
	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, PiecesContainer pieces) {
		super.afterPlace(level, structureManager, chunkGenerator, random, chunkBounds, chunkPos, pieces);
		GroundFill.fillBelow(level, chunkBounds, pieces);
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.QUEST_GROVE.get();
	}

	public static QuestGroveStructure buildStructureConfig(BootstrapContext<Structure> context) {
		return new QuestGroveStructure(
			Optional.of(new DecorationConfig(2, false, true, true)),
			true, Optional.of(TFMapDecorations.QUEST_GROVE),
			new StructureSettings(
				context.lookup(Registries.BIOME).getOrThrow(BiomeTagGenerator.VALID_QUEST_GROVE_BIOMES),
				Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), // Landmarks have Controlled Mob spawning
				GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.BEARD_THIN
			)
		);
	}
}
