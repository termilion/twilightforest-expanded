package twilightforest.world.components.placements;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import twilightforest.init.TFFeatureModifiers;
import twilightforest.util.BoundingBoxUtils;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Drops a feature position that lies within {@code clearance} blocks (largest axis distance) of a piece of one of the given structures.
 * Unlike {@link AvoidLandmarkModifier} this checks the actual pieces of any structure, so irregular structures (e.g. ruin towns) stay clear
 * without a square clearance around them. The clearance should cover the feature's size (spike radius, column reach, pool size).
 */
public class AvoidStructurePiecesModifier extends PlacementModifier {
	public static final MapCodec<AvoidStructurePiecesModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		RegistryCodecs.homogeneousList(Registries.STRUCTURE).fieldOf("structures").forGetter(m -> m.structures),
		// Neighbouring chunks are read for references, which world generation only allows one chunk away
		Codec.intRange(0, 16).fieldOf("clearance").forGetter(m -> m.clearance)
	).apply(instance, AvoidStructurePiecesModifier::new));

	private final HolderSet<Structure> structures;
	private final int clearance;
	// Resolved on first use, the structures are bound only after the registries are loaded
	private final Supplier<Set<Structure>> structureValues;

	public AvoidStructurePiecesModifier(HolderSet<Structure> structures, int clearance) {
		this.structures = structures;
		this.clearance = clearance;
		this.structureValues = Suppliers.memoize(() -> new ReferenceOpenHashSet<>(structures.stream().filter(Holder::isBound).map(Holder::value).toList()));
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
		WorldGenLevel level = context.getLevel();
		for (int chunkX = (pos.getX() - this.clearance) >> 4; chunkX <= (pos.getX() + this.clearance) >> 4; chunkX++) {
			for (int chunkZ = (pos.getZ() - this.clearance) >> 4; chunkZ <= (pos.getZ() + this.clearance) >> 4; chunkZ++) {
				ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_REFERENCES);
				for (Map.Entry<Structure, LongSet> references : chunk.getAllReferences().entrySet()) {
					if (this.structureValues.get().contains(references.getKey()) && this.isNearPiece(level, references.getKey(), references.getValue(), pos))
						return Stream.empty();
				}
			}
		}
		return Stream.of(pos);
	}

	private boolean isNearPiece(WorldGenLevel level, Structure structure, LongSet starts, BlockPos pos) {
		for (long packedStart : starts) {
			ChunkPos startChunk = new ChunkPos(packedStart);
			StructureStart start = level.getChunk(startChunk.x, startChunk.z, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(structure);
			if (start == null || !start.isValid())
				continue;

			for (StructurePiece piece : start.getPieces())
				if (BoundingBoxUtils.greatestAxalDistance(piece.getBoundingBox(), pos) <= this.clearance)
					return true;
		}
		return false;
	}

	@Override
	public PlacementModifierType<?> type() {
		return TFFeatureModifiers.AVOID_STRUCTURE_PIECES.get();
	}
}
