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
import java.util.Optional;
import java.util.function.LongFunction;

/**
 * Grows the given biomes by one pixel into their direct neighbours, making them larger than their original key biome cell.
 * With {@code onlyInto}, they only grow into those biomes (e.g. their border ring) and only where the pixel touches nothing else,
 * so a ring that is only one pixel thick stays intact.
 */
public record ExpandBiomeLayer(List<ResourceKey<Biome>> expandingBiomes, Optional<List<ResourceKey<Biome>>> onlyInto) implements CastleTransformer {
	@Override
	public ResourceKey<Biome> apply(RandomContext randomContext, ResourceKey<Biome> up, ResourceKey<Biome> right, ResourceKey<Biome> down, ResourceKey<Biome> left, ResourceKey<Biome> center) {
		if (this.expandingBiomes.contains(center))
			return center;

		List<ResourceKey<Biome>> neighbours = List.of(up, right, down, left);
		if (this.onlyInto.isPresent()) {
			List<ResourceKey<Biome>> into = this.onlyInto.get();
			if (!into.contains(center))
				return center;
			for (ResourceKey<Biome> neighbour : neighbours)
				if (!into.contains(neighbour) && !this.expandingBiomes.contains(neighbour))
					return center;
		}

		for (ResourceKey<Biome> neighbour : neighbours)
			if (this.expandingBiomes.contains(neighbour))
				return neighbour;

		return center;
	}

	public static final class Factory implements BiomeLayerFactory {
		public static final MapCodec<Factory> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.LONG.fieldOf("salt").forGetter(Factory::salt),
			ResourceKey.codec(Registries.BIOME).listOf().fieldOf("expanding_biomes").forGetter(Factory::expandingBiomes),
			ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("only_into").forGetter(Factory::onlyInto),
			BiomeLayerStack.HOLDER_CODEC.fieldOf("parent").forGetter(Factory::parent)
		).apply(inst, Factory::new));
		private final long salt;
		private final List<ResourceKey<Biome>> expandingBiomes;
		private final Optional<List<ResourceKey<Biome>>> onlyInto;
		private final Holder<BiomeLayerFactory> parent;
		private final ExpandBiomeLayer instance;

		public Factory(long salt, List<ResourceKey<Biome>> expandingBiomes, Holder<BiomeLayerFactory> parent) {
			this(salt, expandingBiomes, Optional.empty(), parent);
		}

		public Factory(long salt, List<ResourceKey<Biome>> expandingBiomes, Optional<List<ResourceKey<Biome>>> onlyInto, Holder<BiomeLayerFactory> parent) {
			this.salt = salt;
			this.expandingBiomes = expandingBiomes;
			this.onlyInto = onlyInto;
			this.parent = parent;

			this.instance = new ExpandBiomeLayer(expandingBiomes, onlyInto);
		}

		@Override
		public LazyArea build(LongFunction<LazyAreaContext> contextFactory) {
			return this.instance.run(contextFactory.apply(this.salt), this.parent.value().build(contextFactory));
		}

		@Override
		public BiomeLayerType getType() {
			return BiomeLayerTypes.EXPAND.get();
		}

		public long salt() {
			return this.salt;
		}

		public List<ResourceKey<Biome>> expandingBiomes() {
			return this.expandingBiomes;
		}

		public Optional<List<ResourceKey<Biome>>> onlyInto() {
			return this.onlyInto;
		}

		public Holder<BiomeLayerFactory> parent() {
			return this.parent;
		}
	}
}
