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
import twilightforest.world.components.layer.vanillalegacy.Area;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerFactory;
import twilightforest.world.components.layer.vanillalegacy.BiomeLayerType;
import twilightforest.world.components.layer.vanillalegacy.area.LazyArea;
import twilightforest.world.components.layer.vanillalegacy.context.LazyAreaContext;
import twilightforest.world.components.layer.vanillalegacy.context.RandomContext;
import twilightforest.world.components.layer.vanillalegacy.traits.AreaTransformer1;
import twilightforest.world.components.layer.vanillalegacy.traits.DimensionOffset0Transformer;

import java.util.List;
import java.util.function.LongFunction;

/**
 * Lets a key biome slot hold several alternatives: every pixel of the placeholder key biome is replaced by a random pick of the variants.
 * Must run right after {@link KeyBiomesLayer} / {@link CompanionBiomesLayer}, while each key biome is still a single pixel.
 */
public record KeyBiomeVariantsLayer(ResourceKey<Biome> placeholder, List<ResourceKey<Biome>> variants) implements AreaTransformer1, DimensionOffset0Transformer {
	@Override
	public ResourceKey<Biome> applyPixel(RandomContext randomContext, Area layer, int x, int z) {
		ResourceKey<Biome> biome = layer.getBiome(x, z);
		return biome == this.placeholder ? this.variants.get(randomContext.nextRandom(this.variants.size())) : biome;
	}

	public static final class Factory implements BiomeLayerFactory {
		public static final MapCodec<Factory> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.LONG.fieldOf("salt").forGetter(Factory::salt),
			ResourceKey.codec(Registries.BIOME).fieldOf("placeholder").forGetter(Factory::placeholder),
			ResourceKey.codec(Registries.BIOME).listOf().fieldOf("variants").forGetter(Factory::variants),
			BiomeLayerStack.HOLDER_CODEC.fieldOf("parent").forGetter(Factory::parent)
		).apply(inst, Factory::new));
		private final long salt;
		private final ResourceKey<Biome> placeholder;
		private final List<ResourceKey<Biome>> variants;
		private final Holder<BiomeLayerFactory> parent;
		private final KeyBiomeVariantsLayer instance;

		public Factory(long salt, ResourceKey<Biome> placeholder, List<ResourceKey<Biome>> variants, Holder<BiomeLayerFactory> parent) {
			this.salt = salt;
			this.placeholder = placeholder;
			this.variants = variants;
			this.parent = parent;

			this.instance = new KeyBiomeVariantsLayer(placeholder, variants);
		}

		@Override
		public LazyArea build(LongFunction<LazyAreaContext> contextFactory) {
			return this.instance.run(contextFactory.apply(this.salt), this.parent.value().build(contextFactory));
		}

		@Override
		public BiomeLayerType getType() {
			return BiomeLayerTypes.KEY_BIOME_VARIANTS.get();
		}

		public long salt() {
			return this.salt;
		}

		public ResourceKey<Biome> placeholder() {
			return this.placeholder;
		}

		public List<ResourceKey<Biome>> variants() {
			return this.variants;
		}

		public Holder<BiomeLayerFactory> parent() {
			return this.parent;
		}
	}
}
