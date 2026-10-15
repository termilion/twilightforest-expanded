package twilightforest.world.components.structures.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

/**
 * Fills the gap between a structure's bottom and the terrain with dirt, so landmarks on uneven ground never float.
 * Meant for {@code Structure#afterPlace}, which runs once per chunk after all pieces were placed.
 */
public final class GroundFill {
	// How far below/above the lowest piece bottom the structure's bottom layer may actually sit (pieces are often placed with a Y offset)
	private static final int SEARCH_BELOW = 4;
	private static final int SEARCH_ABOVE = 2;

	private GroundFill() {
	}

	public static void fillBelow(WorldGenLevel level, BoundingBox chunkBounds, PiecesContainer pieces) {
		int bottom = pieces.pieces().stream().mapToInt(piece -> piece.getBoundingBox().minY()).min().orElse(level.getMinBuildHeight());
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (StructurePiece piece : pieces.pieces()) {
			BoundingBox box = piece.getBoundingBox();
			if (!box.intersects(chunkBounds))
				continue;

			for (int x = Math.max(box.minX(), chunkBounds.minX()); x <= Math.min(box.maxX(), chunkBounds.maxX()); x++)
				for (int z = Math.max(box.minZ(), chunkBounds.minZ()); z <= Math.min(box.maxZ(), chunkBounds.maxZ()); z++)
					fillColumn(level, cursor, x, z, bottom - SEARCH_BELOW, bottom + SEARCH_ABOVE);
		}
	}

	// Bottom-up: the lowest open block with a solid block right above it is the top of the gap; fill downwards until the ground
	private static void fillColumn(WorldGenLevel level, BlockPos.MutableBlockPos cursor, int x, int z, int fromY, int toY) {
		for (int y = fromY; y <= toY; y++) {
			if (!isOpen(level.getBlockState(cursor.set(x, y, z))) || isOpen(level.getBlockState(cursor.set(x, y + 1, z))))
				continue;

			for (cursor.set(x, y, z); cursor.getY() > level.getMinBuildHeight() && isOpen(level.getBlockState(cursor)); cursor.move(Direction.DOWN))
				level.setBlock(cursor, Blocks.DIRT.defaultBlockState(), 2);
			return;
		}
	}

	private static boolean isOpen(BlockState state) {
		return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES);
	}
}
