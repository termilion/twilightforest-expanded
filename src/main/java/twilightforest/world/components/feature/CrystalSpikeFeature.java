package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Based on the vanilla {@link net.minecraft.world.level.levelgen.feature.IceSpikeFeature} with a configurable spike block and ground.
 * Spikes only start on a ground block and only replace air, dirt, ground blocks and their own block.
 * Height is configurable (vanilla: 7-10), the radius follows the vanilla height / 4 rule up to max_radius.
 * The cone sits on a straight column of base_height blocks, one block narrower than its base radius (anchored a few blocks into the ground),
 * instead of the vanilla raised spikes, mirrored lower half and Y 50 pillars.
 * Optionally every spike block touching air or fluid becomes a shell block, and the highest remaining inner blocks a core block.
 */
public class CrystalSpikeFeature extends Feature<CrystalSpikeFeature.Config> {
	private static final int FOUNDATION_DEPTH = 3;

	public CrystalSpikeFeature(Codec<Config> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<Config> context) {
		BlockPos pos = context.origin();
		RandomSource random = context.random();
		WorldGenLevel level = context.level();
		Config config = context.config();

		while (level.isEmptyBlock(pos) && pos.getY() > level.getMinBuildHeight() + 2) {
			pos = pos.below();
		}

		if (!level.getBlockState(pos).is(config.ground())) {
			return false;
		}

		Set<BlockPos> placed = new HashSet<>();
		this.placeOnColumn(level, random, config, pos, config.baseHeight().sample(random), placed);

		this.decorate(level, random, config, placed);
		return true;
	}

	private void placeOnColumn(WorldGenLevel level, RandomSource random, Config config, BlockPos ground, int baseHeight, Set<BlockPos> placed) {
		int height = config.height().sample(random);
		int radius = Math.max(1, Math.min(height / 4 + random.nextInt(2), config.maxRadius()));

		// Column one block narrower on each side than the cone's base, with the same rough outline
		int shaftRadius = Math.max(1, radius - 1);
		for (int y = -FOUNDATION_DEPTH; y <= baseHeight; y++) {
			this.placeLayer(level, random, config, ground.above(y), shaftRadius, shaftRadius, placed);
		}

		BlockPos coneBase = ground.above(baseHeight);
		for (int k = 0; k < height; k++) {
			float f = (1.0F - (float) k / (float) height) * (float) radius;
			this.placeLayer(level, random, config, coneBase.above(k), f, Mth.ceil(f), placed);
		}
	}

	private void placeLayer(WorldGenLevel level, RandomSource random, Config config, BlockPos center, float f, int l, Set<BlockPos> placed) {
		for (int dx = -l; dx <= l; dx++) {
			float fx = (float) Mth.abs(dx) - 0.25F;

			for (int dz = -l; dz <= l; dz++) {
				float fz = (float) Mth.abs(dz) - 0.25F;
				if ((dx == 0 && dz == 0 || !(fx * fx + fz * fz > f * f))
					&& (dx != -l && dx != l && dz != -l && dz != l || !(random.nextFloat() > 0.75F))) {
					this.placeSpikeBlock(level, random, config, center.offset(dx, 0, dz), placed);
				}
			}
		}
	}

	private void placeSpikeBlock(WorldGenLevel level, RandomSource random, Config config, BlockPos pos, Set<BlockPos> placed) {
		if (this.canReplace(level.getBlockState(pos), config)) {
			this.setBlock(level, pos, config.block().getState(random, pos));
			placed.add(pos.immutable());
		}
	}

	/**
	 * Turns the outer layer of the finished spike into the shell block and some of the highest inner blocks into the core block
	 */
	private void decorate(WorldGenLevel level, RandomSource random, Config config, Set<BlockPos> placed) {
		Set<BlockPos> inner = new HashSet<>(placed);

		if (config.shell().isPresent()) {
			List<BlockPos> outer = new ArrayList<>();
			for (BlockPos pos : placed) {
				for (Direction direction : Direction.values()) {
					BlockState neighbour = level.getBlockState(pos.relative(direction));
					if (neighbour.isAir() || !neighbour.getFluidState().isEmpty()) {
						outer.add(pos);
						break;
					}
				}
			}

			for (BlockPos pos : outer)
				this.setBlock(level, pos, config.shell().get().getState(random, pos));
			outer.forEach(inner::remove);
		}

		if (config.core().isPresent() && !inner.isEmpty()) {
			List<BlockPos> highestFirst = new ArrayList<>(inner);
			highestFirst.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
			int count = Math.min(config.coreCount().sample(random), highestFirst.size());
			for (BlockPos pos : highestFirst.subList(0, count))
				this.setBlock(level, pos, config.core().get().getState(random, pos));
		}
	}

	private boolean canReplace(BlockState state, Config config) {
		return state.isAir() || isDirt(state) || state.is(config.ground()) || state.is(config.spikeBlocks());
	}

	/**
	 * @param block       the spike material
	 * @param ground      blocks a spike can start on, also replaced by the spike
	 * @param spikeBlocks blocks the spike consists of (incl. shell and core), so overlapping spikes merge
	 * @param height      spike height above its base
	 * @param maxRadius   cap for the base radius of tall spikes
	 * @param baseHeight  height of the straight column below the cone
	 * @param shell       optional block for every spike block touching air or fluid
	 * @param core        optional block for the highest blocks left inside the shell
	 * @param coreCount   how many of the highest inner blocks become core blocks
	 */
	public record Config(BlockStateProvider block, HolderSet<Block> ground, HolderSet<Block> spikeBlocks, IntProvider height, int maxRadius, IntProvider baseHeight,
						 Optional<BlockStateProvider> shell, Optional<BlockStateProvider> core, IntProvider coreCount) implements FeatureConfiguration {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockStateProvider.CODEC.fieldOf("block").forGetter(Config::block),
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("ground").forGetter(Config::ground),
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("spike_blocks").forGetter(Config::spikeBlocks),
			IntProvider.codec(1, 128).optionalFieldOf("height", UniformInt.of(7, 10)).forGetter(Config::height),
			Codec.intRange(0, 16).optionalFieldOf("max_radius", 16).forGetter(Config::maxRadius),
			IntProvider.codec(1, 128).fieldOf("base_height").forGetter(Config::baseHeight),
			BlockStateProvider.CODEC.optionalFieldOf("shell").forGetter(Config::shell),
			BlockStateProvider.CODEC.optionalFieldOf("core").forGetter(Config::core),
			IntProvider.codec(0, 64).optionalFieldOf("core_count", ConstantInt.ZERO).forGetter(Config::coreCount)
		).apply(instance, Config::new));
	}
}
