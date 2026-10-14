package twilightforest.world.components.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import twilightforest.init.custom.BiomeLayerStack;
import twilightforest.init.custom.BiomeLayerTypes;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerType;
import twilightforest.world.components.layer.vanillalegacy.area.LazyArea;
import twilightforest.world.components.layer.vanillalegacy.context.LazyAreaContext;
import twilightforest.world.components.layer.vanillalegacy.context.RandomContext;
import twilightforest.world.components.layer.vanillalegacy.traits.CastleTransformer;

import java.util.List;
import java.util.function.LongFunction;

/**
 * Opposite of {@link ExpandBiomeLayer}: the given biomes lose their outer pixel to their direct neighbours.
 */
public record ShrinkBiomeLayer(List<ResourceKey<Biome>> shrinkingBiomes) implements CastleTransformer {
	@Override
	public ResourceKey<Biome> apply(RandomContext randomContext, ResourceKey<Biome> up, ResourceKey<Biome> right, ResourceKey<Biome> down, ResourceKey<Biome> left, ResourceKey<Biome> center) {
		if (!this.shrinkingBiomes.contains(center))
			return center;

		for (ResourceKey<Biome> neighbour : List.of(up, right, down, left))
			if (!this.shrinkingBiomes.contains(neighbour))
				return neighbour;

		return center;
	}

	public static final class Factory implements BiomeLayerFactory {
		public static final MapCodec<Factory> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.LONG.fieldOf("salt").forGetter(Factory::salt),
			ResourceKey.codec(Registries.BIOME).listOf().fieldOf("shrinking_biomes").forGetter(Factory::shrinkingBiomes),
			BiomeLayerStack.HOLDER_CODEC.fieldOf("parent").forGetter(Factory::parent)
		).apply(inst, Factory::new));
		private final long salt;
		private final List<ResourceKey<Biome>> shrinkingBiomes;
		private final Holder<BiomeLayerFactory> parent;
		private final ShrinkBiomeLayer instance;

		public Factory(long salt, List<ResourceKey<Biome>> shrinkingBiomes, Holder<BiomeLayerFactory> parent) {
			this.salt = salt;
			this.shrinkingBiomes = shrinkingBiomes;
			this.parent = parent;

			this.instance = new ShrinkBiomeLayer(shrinkingBiomes);
		}

		@Override
		public LazyArea build(LongFunction<LazyAreaContext> contextFactory) {
			return this.instance.run(contextFactory.apply(this.salt), this.parent.value().build(contextFactory));
		}

		@Override
		public BiomeLayerType getType() {
			return BiomeLayerTypes.SHRINK.get();
		}

		public long salt() {
			return this.salt;
		}

		public List<ResourceKey<Biome>> shrinkingBiomes() {
			return this.shrinkingBiomes;
		}

		public Holder<BiomeLayerFactory> parent() {
			return this.parent;
		}
	}
}
