package twilightforest.world.components.structures.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
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
		int bottom = lowestBottom(level, pieces);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		forEachColumn(chunkBounds, pieces, 0, (x, z) -> fillColumn(level, cursor, x, z, bottom - SEARCH_BELOW, bottom + SEARCH_ABOVE));
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

	// Columns around the pieces that are checked too: the cut-away grass can reach a little past a piece's box
	private static final int COVER_MARGIN = 3;

	/**
	 * Replaces dirt with open space above it (air or plants) near the bottom of the structure, i.e. ground whose grass the structure's air blocks
	 * removed, with the cover block. Also for {@code Structure#afterPlace}.
	 */
	public static void coverExposedDirt(WorldGenLevel level, BoundingBox chunkBounds, PiecesContainer pieces, BlockState cover) {
		int bottom = lowestBottom(level, pieces);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();

		forEachColumn(chunkBounds, pieces, COVER_MARGIN, (x, z) -> {
			for (int y = bottom - SEARCH_BELOW; y <= bottom + SEARCH_ABOVE; y++) {
				cursor.set(x, y, z);
				above.set(x, y + 1, z);
				BlockState aboveState = level.getBlockState(above);
				if (level.getBlockState(cursor).is(Blocks.DIRT) && aboveState.canBeReplaced() && aboveState.getFluidState().isEmpty())
					level.setBlock(cursor, cover, Block.UPDATE_CLIENTS);
			}
		});
	}

	private static int lowestBottom(WorldGenLevel level, PiecesContainer pieces) {
		return pieces.pieces().stream().mapToInt(piece -> piece.getBoundingBox().minY()).min().orElse(level.getMinBuildHeight());
	}

	// Runs the action once for every column of the chunk within margin blocks of a piece, even where pieces overlap
	private static void forEachColumn(BoundingBox chunkBounds, PiecesContainer pieces, int margin, IntBiConsumer action) {
		int sizeZ = chunkBounds.getZSpan();
		boolean[] covered = new boolean[chunkBounds.getXSpan() * sizeZ];
		for (StructurePiece piece : pieces.pieces()) {
			BoundingBox box = piece.getBoundingBox().inflatedBy(margin);
			if (!box.intersects(chunkBounds))
				continue;

			for (int x = Math.max(box.minX(), chunkBounds.minX()); x <= Math.min(box.maxX(), chunkBounds.maxX()); x++)
				for (int z = Math.max(box.minZ(), chunkBounds.minZ()); z <= Math.min(box.maxZ(), chunkBounds.maxZ()); z++)
					covered[(x - chunkBounds.minX()) * sizeZ + z - chunkBounds.minZ()] = true;
		}

		for (int i = 0; i < covered.length; i++)
			if (covered[i])
				action.accept(chunkBounds.minX() + i / sizeZ, chunkBounds.minZ() + i % sizeZ);
	}

	@FunctionalInterface
	private interface IntBiConsumer {
		void accept(int x, int z);
	}
}
