package twilightforest.world.components.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import twilightforest.init.custom.BiomeLayerStack;
import twilightforest.init.custom.BiomeLayerTypes;
import twilightforest.util.landmarks.LegacyLandmarkPlacements;
import twilightforest.world.components.layer.vanillalegacy.Area;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerType;
import twilightforest.world.components.layer.vanillalegacy.area.LazyArea;
import twilightforest.world.components.layer.vanillalegacy.context.LazyAreaContext;
import twilightforest.world.components.layer.vanillalegacy.context.RandomContext;
import twilightforest.world.components.layer.vanillalegacy.traits.AreaTransformer1;
import twilightforest.world.components.layer.vanillalegacy.traits.DimensionOffset0Transformer;

import java.util.Optional;
import java.util.function.LongFunction;

/**
 * Runs at full (quart) resolution. Where a landmark center lies inside the host biome (e.g. a lake), an island of the island biome is placed on it.
 * The island spans from the landmark grid tile center (where {@code LandmarkStructure} checks the biome) to the jittered landmark center
 * (where the structure is built), so the island's landmark always generates on it.
 * With a grid parity, only landmark centers whose grid tile X coordinate has that parity (0 or 1) get an island, i.e. every other
 * column of landmark centers; the others keep the host biome and its landmark. Columns instead of a checkerboard: of the four tiles
 * orthogonally next to any tile, two lie in each column parity, so a host biome ring around a key biome always keeps some of its landmarks.
 */
public record LandmarkIslandLayer(ResourceKey<Biome> hostBiome, ResourceKey<Biome> islandBiome, int radius, Optional<Integer> gridParity) implements AreaTransformer1, DimensionOffset0Transformer {
	@Override
	public ResourceKey<Biome> applyPixel(RandomContext randomContext, Area layer, int x, int z) {
		ResourceKey<Biome> biome = layer.getBiome(x, z);
		if (biome != this.hostBiome)
			return biome;

		int blockX = (x << 2) + 2;
		int blockZ = (z << 2) + 2;
		BlockPos center = LegacyLandmarkPlacements.getNearestCenterXZ(blockX >> 4, blockZ >> 4);
		// Same tile center LandmarkStructure#findValidGenerationPoint samples
		int gridX = (Math.round((center.getX() >> 4) / 16F) << 8) + 8;
		int gridZ = (Math.round((center.getZ() >> 4) / 16F) << 8) + 8;

		if (this.gridParity.isPresent() && ((gridX >> 8) & 1) != this.gridParity.get())
			return biome;

		if (distanceSqToSegment(blockX, blockZ, gridX, gridZ, center.getX(), center.getZ()) > this.radius * this.radius)
			return biome;

		if (layer.getBiome(center.getX() >> 2, center.getZ() >> 2) != this.hostBiome || layer.getBiome(gridX >> 2, gridZ >> 2) != this.hostBiome)
			return biome;

		return this.islandBiome;
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

	public static final class Factory implements BiomeLayerFactory {
		public static final MapCodec<Factory> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.LONG.fieldOf("salt").forGetter(Factory::salt),
			ResourceKey.codec(Registries.BIOME).fieldOf("host_biome").forGetter(Factory::hostBiome),
			ResourceKey.codec(Registries.BIOME).fieldOf("island_biome").forGetter(Factory::islandBiome),
			Codec.intRange(1, 256).fieldOf("radius").forGetter(Factory::radius),
			Codec.intRange(0, 1).optionalFieldOf("grid_parity").forGetter(Factory::gridParity),
			BiomeLayerStack.HOLDER_CODEC.fieldOf("parent").forGetter(Factory::parent)
		).apply(inst, Factory::new));
		private final long salt;
		private final ResourceKey<Biome> hostBiome;
		private final ResourceKey<Biome> islandBiome;
		private final int radius;
		private final Optional<Integer> gridParity;
		private final Holder<BiomeLayerFactory> parent;
		private final LandmarkIslandLayer instance;

		public Factory(long salt, ResourceKey<Biome> hostBiome, ResourceKey<Biome> islandBiome, int radius, Optional<Integer> gridParity, Holder<BiomeLayerFactory> parent) {
			this.salt = salt;
			this.hostBiome = hostBiome;
			this.islandBiome = islandBiome;
			this.radius = radius;
			this.gridParity = gridParity;
			this.parent = parent;

			this.instance = new LandmarkIslandLayer(hostBiome, islandBiome, radius, gridParity);
		}

		@Override
		public LazyArea build(LongFunction<LazyAreaContext> contextFactory) {
			return this.instance.run(contextFactory.apply(this.salt), this.parent.value().build(contextFactory));
		}

		@Override
		public BiomeLayerType getType() {
			return BiomeLayerTypes.LANDMARK_ISLAND.get();
		}

		public long salt() {
			return this.salt;
		}

		public ResourceKey<Biome> hostBiome() {
			return this.hostBiome;
		}

		public ResourceKey<Biome> islandBiome() {
			return this.islandBiome;
		}

		public int radius() {
			return this.radius;
		}

		public Optional<Integer> gridParity() {
			return this.gridParity;
		}

		public Holder<BiomeLayerFactory> parent() {
			return this.parent;
		}
	}
}
