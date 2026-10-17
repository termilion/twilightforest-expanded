package twilightforest.world.components.structures.peakshrine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.world.components.structures.TFStructureComponentOld;

/**
 * Placeholder for the summit structure: a round packed ice platform with four ice pillars and the Ashen Lord spawner in the middle.
 */
public class PeakShrineComponent extends TFStructureComponentOld {

	private static final int RADIUS = 9;
	private static final int HEIGHT = 8;

	public PeakShrineComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFPeakShrine.get(), nbt);
	}

	public PeakShrineComponent(int index, int x, int y, int z) {
		super(TFStructurePieceTypes.TFPeakShrine.get(), index, new BoundingBox(x - RADIUS, y - 2, z - RADIUS, x + RADIUS, y + HEIGHT, z + RADIUS));
		// null orientation would make placeBlock use absolute coordinates, the platform is symmetric so any direction works
		this.setOrientation(Direction.NORTH);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator, RandomSource random, BoundingBox writeableBounds, ChunkPos chunkPos, BlockPos pos) {
		BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
		BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				int distSq = dx * dx + dz * dz;
				if (distSq > RADIUS * RADIUS) continue;

				int x = RADIUS + dx;
				int z = RADIUS + dz;
				// floor, edge ring in blue ice
				this.placeBlock(level, Blocks.SNOW_BLOCK.defaultBlockState(), x, 0, z, writeableBounds);
				this.placeBlock(level, distSq > (RADIUS - 1) * (RADIUS - 1) ? blueIce : ice, x, 1, z, writeableBounds);
				// clear the arena above
				for (int y = 2; y <= HEIGHT + 2; y++) {
					this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, writeableBounds);
				}
			}
		}

		// four pillars
		for (int[] corner : new int[][]{{-6, -6}, {-6, 6}, {6, -6}, {6, 6}}) {
			for (int y = 2; y <= 6; y++) {
				this.placeBlock(level, y == 6 ? Blocks.SEA_LANTERN.defaultBlockState() : ice, RADIUS + corner[0], y, RADIUS + corner[1], writeableBounds);
			}
		}

		this.placeBlock(level, TFBlocks.ASHEN_LORD_BOSS_SPAWNER.get().defaultBlockState(), RADIUS, 2, RADIUS, writeableBounds);
	}
}
