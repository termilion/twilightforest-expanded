package twilightforest.world.components.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFStructurePieceTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * A 5x5 spiral staircase connecting a surface entrance with an underground structure.
 * The inner 3x3 holds a central pillar with one step per level circling around it (one full turn every 8 blocks),
 * the outer ring is walled with stone bricks, except where the shaft passes through other structure pieces.
 */
public class SpiralStairShaftPiece extends StructurePiece {
	// Clockwise ring around the pillar, starting in the north-west corner
	private static final int[][] RING = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};

	private final int centerX;
	private final int centerZ;
	private final int topY;
	private final int bottomY;
	private final int wallTopY;
	private final List<int[]> openRanges;

	/**
	 * @param topY       height of the uppermost step, usually flush with the entrance floor
	 * @param bottomY    height of the lowermost step, usually the floor level of the room the shaft leads into
	 * @param wallTopY   walls are only built up to this height, so they don't intrude into the entrance building
	 * @param openRanges inclusive [minY, maxY] ranges where the shaft passes through other pieces and must not be walled
	 */
	public SpiralStairShaftPiece(int centerX, int centerZ, int topY, int bottomY, int wallTopY, List<int[]> openRanges) {
		super(TFStructurePieceTypes.SPIRAL_STAIR_SHAFT.value(), 0, new BoundingBox(centerX - 2, bottomY, centerZ - 2, centerX + 2, topY, centerZ + 2));
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.topY = topY;
		this.bottomY = bottomY;
		this.wallTopY = wallTopY;
		this.openRanges = openRanges;
	}

	public SpiralStairShaftPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
		super(TFStructurePieceTypes.SPIRAL_STAIR_SHAFT.value(), tag);
		this.centerX = tag.getInt("center_x");
		this.centerZ = tag.getInt("center_z");
		this.topY = tag.getInt("top_y");
		this.bottomY = tag.getInt("bottom_y");
		this.wallTopY = tag.getInt("wall_top_y");
		this.openRanges = new ArrayList<>();
		for (Tag range : tag.getList("open_ranges", Tag.TAG_INT_ARRAY))
			this.openRanges.add(((IntArrayTag) range).getAsIntArray());
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putInt("center_x", this.centerX);
		tag.putInt("center_z", this.centerZ);
		tag.putInt("top_y", this.topY);
		tag.putInt("bottom_y", this.bottomY);
		tag.putInt("wall_top_y", this.wallTopY);
		ListTag ranges = new ListTag();
		for (int[] range : this.openRanges)
			ranges.add(new IntArrayTag(range));
		tag.put("open_ranges", ranges);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, BlockPos blockPos) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int y = this.topY; y >= this.bottomY; y--) {
			boolean walled = y <= this.wallTopY && !this.isOpen(y);

			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					pos.set(this.centerX + dx, y, this.centerZ + dz);
					if (!chunkBounds.isInside(pos))
						continue;

					if (dx == 0 && dz == 0) {
						level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
					} else if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
						if (walled)
							level.setBlock(pos, randomWall(random), Block.UPDATE_CLIENTS);
					} else {
						level.setBlock(pos, this.stepOrAir(dx, dz, y), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}

	private BlockState stepOrAir(int dx, int dz, int y) {
		int step = Math.floorMod(this.topY - y, RING.length);
		if (RING[step][0] != dx || RING[step][1] != dz)
			return Blocks.AIR.defaultBlockState();

		// The stair rises towards the previous, higher step. Neighbouring ring cells always share an edge
		int[] previous = RING[Math.floorMod(step - 1, RING.length)];
		Direction facing = Direction.fromDelta(previous[0] - dx, 0, previous[1] - dz);

		return Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
	}

	private boolean isOpen(int y) {
		for (int[] range : this.openRanges)
			if (y >= range[0] && y <= range[1])
				return true;
		return false;
	}

	private static BlockState randomWall(RandomSource random) {
		return switch (random.nextInt(6)) {
			case 0 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
			case 1 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
			default -> Blocks.STONE_BRICKS.defaultBlockState();
		};
	}
}
