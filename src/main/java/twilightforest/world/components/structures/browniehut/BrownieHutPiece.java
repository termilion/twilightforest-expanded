package twilightforest.world.components.structures.browniehut;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFEntities;
import twilightforest.init.TFStructurePieceTypes;

/**
 * Copy of the vanilla swamp hut ({@code SwampHutPiece}) with a brownie living in it instead of the witch; the black cat stays.
 */
public class BrownieHutPiece extends ScatteredFeaturePiece {
	private boolean spawnedBrownie;
	private boolean spawnedCat;

	public BrownieHutPiece(RandomSource random, int x, int z) {
		super(TFStructurePieceTypes.TFBrownieHut.get(), x, 64, z, 7, 7, 9, getRandomHorizontalDirection(random));
	}

	public BrownieHutPiece(StructurePieceSerializationContext context, CompoundTag tag) {
		super(TFStructurePieceTypes.TFBrownieHut.get(), tag);
		this.spawnedBrownie = tag.getBoolean("Brownie");
		this.spawnedCat = tag.getBoolean("Cat");
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putBoolean("Brownie", this.spawnedBrownie);
		tag.putBoolean("Cat", this.spawnedCat);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
		if (!this.updateAverageGroundHeight(level, box, 0))
			return;

		BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		this.generateBox(level, box, 1, 1, 1, 5, 1, 7, planks, planks, false);
		this.generateBox(level, box, 1, 4, 2, 5, 4, 7, planks, planks, false);
		this.generateBox(level, box, 2, 1, 0, 4, 1, 0, planks, planks, false);
		this.generateBox(level, box, 2, 2, 2, 3, 3, 2, planks, planks, false);
		this.generateBox(level, box, 1, 2, 3, 1, 3, 6, planks, planks, false);
		this.generateBox(level, box, 5, 2, 3, 5, 3, 6, planks, planks, false);
		this.generateBox(level, box, 2, 2, 7, 4, 3, 7, planks, planks, false);
		this.generateBox(level, box, 1, 0, 2, 1, 3, 2, log, log, false);
		this.generateBox(level, box, 5, 0, 2, 5, 3, 2, log, log, false);
		this.generateBox(level, box, 1, 0, 7, 1, 3, 7, log, log, false);
		this.generateBox(level, box, 5, 0, 7, 5, 3, 7, log, log, false);
		this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, box);
		this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, box);
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 1, 3, 4, box);
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 5, 3, 4, box);
		this.placeBlock(level, Blocks.AIR.defaultBlockState(), 5, 3, 5, box);
		this.placeBlock(level, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, box);
		this.placeBlock(level, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, box);
		this.placeBlock(level, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, box);
		this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, box);
		this.placeBlock(level, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, box);
		BlockState north = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
		BlockState east = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
		BlockState west = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
		BlockState south = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
		this.generateBox(level, box, 0, 4, 1, 6, 4, 1, north, north, false);
		this.generateBox(level, box, 0, 4, 2, 0, 4, 7, east, east, false);
		this.generateBox(level, box, 6, 4, 2, 6, 4, 7, west, west, false);
		this.generateBox(level, box, 0, 4, 8, 6, 4, 8, south, south, false);
		this.placeBlock(level, north.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, box);
		this.placeBlock(level, north.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, box);
		this.placeBlock(level, south.setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, box);
		this.placeBlock(level, south.setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, box);

		for (int z = 2; z <= 7; z += 5)
			for (int x = 1; x <= 5; x += 4)
				this.fillColumnDown(level, log, x, -1, z, box);

		if (!this.spawnedBrownie)
			this.spawnedBrownie = this.spawn(level, box, TFEntities.BROWNIE.get());
		if (!this.spawnedCat)
			this.spawnedCat = this.spawn(level, box, EntityType.CAT);
	}

	private boolean spawn(WorldGenLevel level, BoundingBox box, EntityType<? extends Mob> type) {
		BlockPos pos = this.getWorldPos(2, 2, 5);
		if (!box.isInside(pos))
			return false;

		Mob mob = type.create(level.getLevel());
		if (mob != null) {
			mob.setPersistenceRequired();
			mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			level.addFreshEntityWithPassengers(mob);
		}
		return true;
	}
}
