package twilightforest.world.components.chunkgenerators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.DensityFunction;
import twilightforest.TFRegistries;
import twilightforest.util.landmarks.LegacyLandmarkPlacements;
import twilightforest.world.components.layer.BiomeDensitySource;

import java.util.List;

/**
 * Mountain shape around landmark centers: 1 on a plateau around the landmark, falling to 0 at the mountain foot.
 * Only landmark grid tiles whose main biome is one of the host biomes get a mountain. Purely 2D, wrap it in a flat cache.
 */
public record LandmarkPeakFunction(Holder<BiomeDensitySource> terrainSource, List<ResourceKey<Biome>> hostBiomes, double radius, double plateauRadius, double exponent) implements DensityFunction.SimpleFunction {
	public static final MapCodec<LandmarkPeakFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		RegistryFileCodec.create(TFRegistries.Keys.BIOME_TERRAIN_DATA, BiomeDensitySource.CODEC, false).fieldOf("terrain_source").forGetter(LandmarkPeakFunction::terrainSource),
		ResourceKey.codec(Registries.BIOME).listOf().fieldOf("host_biomes").forGetter(LandmarkPeakFunction::hostBiomes),
		Codec.doubleRange(1, 512).fieldOf("radius").forGetter(LandmarkPeakFunction::radius),
		Codec.doubleRange(0, 256).fieldOf("plateau_radius").forGetter(LandmarkPeakFunction::plateauRadius),
		Codec.doubleRange(0.1, 8).fieldOf("exponent").forGetter(LandmarkPeakFunction::exponent)
	).apply(inst, LandmarkPeakFunction::new));
	public static final KeyDispatchDataCodec<LandmarkPeakFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

	@Override
	public double compute(FunctionContext context) {
		int blockX = context.blockX();
		int blockZ = context.blockZ();
		int tileX = Math.round((blockX >> 4) / 16F);
		int tileZ = Math.round((blockZ >> 4) / 16F);

		double shape = 0;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				shape = Math.max(shape, this.shapeForTile((tileX + dx) << 4, (tileZ + dz) << 4, blockX, blockZ));
			}
		}
		return shape;
	}

	private double shapeForTile(int tileChunkX, int tileChunkZ, int blockX, int blockZ) {
		// Same grid tile center and landmark center LandmarkIslandLayer uses for the summit clearing
		int gridX = LegacyLandmarkPlacements.getTileCenterQuart(tileChunkX) << 2;
		int gridZ = LegacyLandmarkPlacements.getTileCenterQuart(tileChunkZ) << 2;

		double maxDistSq = this.radius * this.radius;
		double gdx = blockX - gridX;
		double gdz = blockZ - gridZ;
		// the landmark center is at most a few chunks away from the grid tile center
		if (gdx * gdx + gdz * gdz > (this.radius + 64) * (this.radius + 64))
			return 0;

		if (!this.hostBiomes.contains(this.terrainSource.value().getBiomeKey(gridX >> 2, gridZ >> 2)))
			return 0;

		BlockPos center = LegacyLandmarkPlacements.getNearestCenterXZ(tileChunkX, tileChunkZ);
		double distSq = distanceSqToSegment(blockX, blockZ, gridX, gridZ, center.getX(), center.getZ());
		if (distSq >= maxDistSq)
			return 0;

		double dist = Math.sqrt(distSq);
		if (dist <= this.plateauRadius)
			return 1;

		double t = (dist - this.plateauRadius) / (this.radius - this.plateauRadius);
		return Math.pow(1 - Mth.clamp(t, 0, 1), this.exponent);
	}

	private static double distanceSqToSegment(double px, double pz, double ax, double az, double bx, double bz) {
		double dx = bx - ax;
		double dz = bz - az;
		double lengthSq = dx * dx + dz * dz;
		double t = lengthSq == 0 ? 0 : Math.clamp(((px - ax) * dx + (pz - az) * dz) / lengthSq, 0, 1);
		double ox = px - (ax + t * dx);
		double oz = pz - (az + t * dz);
		return ox * ox + oz * oz;
	}

	@Override
	public double minValue() {
		return 0;
	}

	@Override
	public double maxValue() {
		return 1;
	}

	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		return KEY_CODEC;
	}
}
