package twilightforest.world.components.structures.lakeruin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.loot.TFLootTables;

/**
 * Vanilla (cold) underwater ruin template placed on the lake floor, see {@code OceanRuinPieces.OceanRuinPiece}.
 * Chests roll the hedge maze loot table instead of the vanilla underwater ruin tables.
 */
public class LakeRuinPiece extends TemplateStructurePiece {
	private static final ResourceKey<LootTable> CHEST_LOOT = TFLootTables.HEDGE_MAZE;
	private final float integrity;

	public LakeRuinPiece(StructureTemplateManager templateManager, ResourceLocation location, BlockPos pos, Rotation rotation, float integrity) {
		super(TFStructurePieceTypes.TFLakeRuin.get(), 0, templateManager, location, location.toString(), makeSettings(rotation, integrity), pos);
		this.integrity = integrity;
	}

	public LakeRuinPiece(StructurePieceSerializationContext context, CompoundTag tag) {
		super(TFStructurePieceTypes.TFLakeRuin.get(), tag, context.structureTemplateManager(), location -> makeSettings(Rotation.valueOf(tag.getString("Rot")), tag.getFloat("Integrity")));
		this.integrity = tag.getFloat("Integrity");
	}

	private static StructurePlaceSettings makeSettings(Rotation rotation, float integrity) {
		return new StructurePlaceSettings()
			.setRotation(rotation)
			.setMirror(Mirror.NONE)
			.addProcessor(new BlockRotProcessor(integrity))
			.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putString("Rot", this.placeSettings.getRotation().name());
		tag.putFloat("Integrity", this.integrity);
	}

	@Override
	protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
		if ("chest".equals(name)) {
			level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, level.getFluidState(pos).is(FluidTags.WATER)), 2);
			if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest)
				chest.setLootTable(CHEST_LOOT, random.nextLong());
		} else if ("drowned".equals(name)) {
			Drowned drowned = EntityType.DROWNED.create(level.getLevel());
			if (drowned != null) {
				drowned.setPersistenceRequired();
				drowned.moveTo(pos, 0.0F, 0.0F);
				drowned.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
				level.addFreshEntityWithPassengers(drowned);
			}
			level.setBlock(pos, pos.getY() > level.getSeaLevel() ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
		}
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
		int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ());
		this.templatePosition = new BlockPos(this.templatePosition.getX(), floor, this.templatePosition.getZ());
		BlockPos farCorner = StructureTemplate.transform(new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), Mirror.NONE, this.placeSettings.getRotation(), BlockPos.ZERO).offset(this.templatePosition);
		this.templatePosition = new BlockPos(this.templatePosition.getX(), this.getHeight(this.templatePosition, level, farCorner), this.templatePosition.getZ());
		super.postProcess(level, structureManager, generator, random, box, chunkPos, pos);
	}

	// Copy of OceanRuinPiece#getHeight: sink the ruin into the floor where the ground under it drops away
	private int getHeight(BlockPos templatePos, BlockGetter level, BlockPos farCorner) {
		int y = templatePos.getY();
		int lowest = 512;
		int limit = y - 1;
		int dropped = 0;

		for (BlockPos column : BlockPos.betweenClosed(templatePos, farCorner)) {
			int x = column.getX();
			int z = column.getZ();
			int floorY = templatePos.getY() - 1;
			BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, floorY, z);
			BlockState state = level.getBlockState(cursor);

			for (FluidState fluid = level.getFluidState(cursor); (state.isAir() || fluid.is(FluidTags.WATER) || state.is(BlockTags.ICE)) && floorY > level.getMinBuildHeight() + 1; fluid = level.getFluidState(cursor)) {
				cursor.set(x, --floorY, z);
				state = level.getBlockState(cursor);
			}

			lowest = Math.min(lowest, floorY);
			if (floorY < limit - 2)
				dropped++;
		}

		int width = Math.abs(templatePos.getX() - farCorner.getX());
		if (limit - lowest > 2 && dropped > width - 2)
			y = lowest + 1;

		return y;
	}
}
