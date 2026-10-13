package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
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

/**
 * Copy of the vanilla {@link net.minecraft.world.level.levelgen.feature.IceSpikeFeature} with a configurable spike block and ground.
 * Spikes only start on a ground block and only replace air, dirt, ground blocks and their own block.
 * Height is configurable (vanilla: 7-10), the radius follows the vanilla height / 4 rule up to max_radius.
 * With a base_height, the cone sits on a straight column one block narrower than its base radius instead (anchored a few blocks into the ground),
 * replacing the vanilla raised spikes, mirrored lower half and Y 50 pillars.
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

		int baseHeight = config.baseHeight().sample(random);
		if (baseHeight > 0) {
			return this.placeOnColumn(level, random, config, pos, baseHeight);
		}

		pos = pos.above(random.nextInt(4));
		int height = config.height().sample(random);
		int radius = Math.min(height / 4 + random.nextInt(2), config.maxRadius());
		if (radius > 1 && random.nextInt(60) == 0) {
			pos = pos.above(10 + random.nextInt(30));
		}

		for (int k = 0; k < height; k++) {
			float f = (1.0F - (float) k / (float) height) * (float) radius;
			int l = Mth.ceil(f);

			for (int dx = -l; dx <= l; dx++) {
				float fx = (float) Mth.abs(dx) - 0.25F;

				for (int dz = -l; dz <= l; dz++) {
					float fz = (float) Mth.abs(dz) - 0.25F;
					if ((dx == 0 && dz == 0 || !(fx * fx + fz * fz > f * f))
						&& (dx != -l && dx != l && dz != -l && dz != l || !(random.nextFloat() > 0.75F))) {
						BlockPos upper = pos.offset(dx, k, dz);
						if (this.canReplace(level.getBlockState(upper), config)) {
							this.setBlock(level, upper, config.block().getState(random, upper));
						}

						if (k != 0 && l > 1) {
							BlockPos lower = pos.offset(dx, -k, dz);
							if (this.canReplace(level.getBlockState(lower), config)) {
								this.setBlock(level, lower, config.block().getState(random, lower));
							}
						}
					}
				}
			}
		}

		int baseRadius = Mth.clamp(radius - 1, 0, 1);

		for (int dx = -baseRadius; dx <= baseRadius; dx++) {
			for (int dz = -baseRadius; dz <= baseRadius; dz++) {
				BlockPos base = pos.offset(dx, -1, dz);
				int depth = 50;
				if (Math.abs(dx) == 1 && Math.abs(dz) == 1) {
					depth = random.nextInt(5);
				}

				while (base.getY() > 50) {
					if (!this.canReplace(level.getBlockState(base), config)) {
						break;
					}

					this.setBlock(level, base, config.block().getState(random, base));
					base = base.below();
					if (--depth <= 0) {
						base = base.below(random.nextInt(5) + 1);
						depth = random.nextInt(5);
					}
				}
			}
		}

		return true;
	}

	private boolean placeOnColumn(WorldGenLevel level, RandomSource random, Config config, BlockPos ground, int baseHeight) {
		int height = config.height().sample(random);
		int radius = Math.max(1, Math.min(height / 4 + random.nextInt(2), config.maxRadius()));

		// Column one block narrower on each side than the cone's base, with the same rough outline
		int shaftRadius = Math.max(1, radius - 1);
		for (int y = -FOUNDATION_DEPTH; y <= baseHeight; y++) {
			this.placeLayer(level, random, config, ground.above(y), shaftRadius, shaftRadius);
		}

		BlockPos coneBase = ground.above(baseHeight);
		for (int k = 0; k < height; k++) {
			float f = (1.0F - (float) k / (float) height) * (float) radius;
			this.placeLayer(level, random, config, coneBase.above(k), f, Mth.ceil(f));
		}

		return true;
	}

	private void placeLayer(WorldGenLevel level, RandomSource random, Config config, BlockPos center, float f, int l) {
		for (int dx = -l; dx <= l; dx++) {
			float fx = (float) Mth.abs(dx) - 0.25F;

			for (int dz = -l; dz <= l; dz++) {
				float fz = (float) Mth.abs(dz) - 0.25F;
				if ((dx == 0 && dz == 0 || !(fx * fx + fz * fz > f * f))
					&& (dx != -l && dx != l && dz != -l && dz != l || !(random.nextFloat() > 0.75F))) {
					BlockPos target = center.offset(dx, 0, dz);
					if (this.canReplace(level.getBlockState(target), config)) {
						this.setBlock(level, target, config.block().getState(random, target));
					}
				}
			}
		}
	}

	private boolean canReplace(BlockState state, Config config) {
		return state.isAir() || isDirt(state) || state.is(config.ground()) || state.is(config.spikeBlocks());
	}

	/**
	 * @param block       the spike material
	 * @param ground      blocks a spike can start on, also replaced by the spike
	 * @param spikeBlocks blocks the spike consists of, so overlapping spikes merge
	 * @param height      spike height above its base
	 * @param maxRadius   cap for the base radius of tall spikes
	 * @param baseHeight  height of the straight column below the cone, 0 for the vanilla ice spike shape
	 */
	public record Config(BlockStateProvider block, HolderSet<Block> ground, HolderSet<Block> spikeBlocks, IntProvider height, int maxRadius, IntProvider baseHeight) implements FeatureConfiguration {
		public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockStateProvider.CODEC.fieldOf("block").forGetter(Config::block),
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("ground").forGetter(Config::ground),
			RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("spike_blocks").forGetter(Config::spikeBlocks),
			IntProvider.codec(1, 128).optionalFieldOf("height", UniformInt.of(7, 10)).forGetter(Config::height),
			Codec.intRange(0, 16).optionalFieldOf("max_radius", 16).forGetter(Config::maxRadius),
			IntProvider.codec(0, 128).optionalFieldOf("base_height", ConstantInt.ZERO).forGetter(Config::baseHeight)
		).apply(instance, Config::new));
	}
}
