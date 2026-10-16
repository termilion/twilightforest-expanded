package twilightforest.loot.modifiers;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import twilightforest.init.TFDimension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Adjusts the loot of containers (chests, barrels, ...) in the Twilight Forest by the biome or structure they sit in.
 * One modifier with an ordered list of rules: the container check, biome and structure lookups run once per container, then every matching
 * rule applies in list order (so later emerald rules win over earlier ones).
 */
public class ChestLootRulesModifier extends LootModifier {
	// Loot tables that use the chest parameter set but aren't containers
	private static final List<String> EXCLUDED_PATH_PARTS = List.of("entities/", "blocks/", "gameplay/", "archaeology", "spawner", "equipment/", "keys/", "pots/", "dispenser", "rewards/", "items/", "shearing/", "_rack", "dropper", "useless");

	public static final MapCodec<ChestLootRulesModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> LootModifier.codecStart(inst)
		.and(Rule.CODEC.listOf().fieldOf("rules").forGetter(m -> m.rules))
		.apply(inst, ChestLootRulesModifier::new));

	private final List<Rule> rules;

	public ChestLootRulesModifier(LootItemCondition[] conditions, List<Rule> rules) {
		super(conditions);
		this.rules = rules;
	}

	private static boolean isTwilightContainer(LootContext context) {
		if (!context.getLevel().dimension().equals(TFDimension.DIMENSION_KEY)) return false;
		if (!context.hasParam(LootContextParams.ORIGIN) || context.hasParam(LootContextParams.BLOCK_STATE) || context.hasParam(LootContextParams.DAMAGE_SOURCE) || context.hasParam(LootContextParams.TOOL))
			return false;
		String path = context.getQueriedLootTableId().getPath();
		if (path.endsWith("_pot")) return false;
		for (String part : EXCLUDED_PATH_PARTS) {
			if (path.contains(part)) return false;
		}
		return true;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (!isTwilightContainer(context)) return generatedLoot;
		ServerLevel level = context.getLevel();
		BlockPos pos = BlockPos.containing(context.getParam(LootContextParams.ORIGIN));
		Holder<Biome> biome = level.getBiome(pos);
		Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		Map<ResourceKey<Structure>, Boolean> insideStructure = new HashMap<>();
		Predicate<ResourceKey<Structure>> isInside = key -> insideStructure.computeIfAbsent(key, k -> {
			Structure structure = structures.get(k);
			return structure != null && level.structureManager().getStructureWithPieceAt(pos, structure).isValid();
		});
		RandomSource random = context.getRandom();

		for (Rule rule : this.rules)
			if (rule.matches(biome, isInside))
				rule.apply(generatedLoot, random);

		generatedLoot.removeIf(ItemStack::isEmpty);
		return generatedLoot;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}

	/**
	 * Matches if the container is in one of {@code biomes} (ids or {@code #tags}) or inside a piece of one of {@code structures};
	 * with neither given it matches every container. Order inside one rule: thin, then set_count, then add.
	 *
	 * @param name only for reading the data file
	 */
	public record Rule(String name, List<Either<TagKey<Biome>, ResourceKey<Biome>>> biomes, List<ResourceKey<Structure>> structures, List<Thinning> thin, List<SetCount> setCounts, List<Addition> additions) {
		public static final Codec<Rule> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.STRING.optionalFieldOf("name", "").forGetter(Rule::name),
			Codec.either(TagKey.hashedCodec(Registries.BIOME), ResourceKey.codec(Registries.BIOME)).listOf().optionalFieldOf("biomes", List.of()).forGetter(Rule::biomes),
			ResourceKey.codec(Registries.STRUCTURE).listOf().optionalFieldOf("structures", List.of()).forGetter(Rule::structures),
			Thinning.CODEC.listOf().optionalFieldOf("thin", List.of()).forGetter(Rule::thin),
			SetCount.CODEC.listOf().optionalFieldOf("set_count", List.of()).forGetter(Rule::setCounts),
			Addition.CODEC.listOf().optionalFieldOf("add", List.of()).forGetter(Rule::additions)
		).apply(inst, Rule::new));

		private boolean matches(Holder<Biome> biome, Predicate<ResourceKey<Structure>> isInsideStructure) {
			if (this.biomes.isEmpty() && this.structures.isEmpty()) return true;
			for (Either<TagKey<Biome>, ResourceKey<Biome>> entry : this.biomes) {
				if (entry.map(biome::is, biome::is)) return true;
			}
			return this.structures.stream().anyMatch(isInsideStructure);
		}

		private void apply(ObjectArrayList<ItemStack> loot, RandomSource random) {
			for (Thinning thinning : this.thin) {
				for (ItemStack stack : loot) {
					if (!stack.is(thinning.item())) continue;
					int kept = 0;
					for (int i = 0; i < stack.getCount(); i++) {
						if (random.nextFloat() < thinning.keepChance()) kept++;
					}
					stack.setCount(kept);
				}
			}

			for (SetCount setCount : this.setCounts) {
				loot.forEach(stack -> {
					if (stack.is(setCount.item())) stack.setCount(0);
				});
				addStacks(loot, setCount.item(), random.nextIntBetweenInclusive(setCount.min(), setCount.max()));
			}

			for (Addition addition : this.additions) {
				if (random.nextFloat() < addition.chance())
					addStacks(loot, addition.item(), random.nextIntBetweenInclusive(addition.min(), addition.max()));
			}
		}

		private static void addStacks(ObjectArrayList<ItemStack> loot, Item item, int count) {
			int maxStack = new ItemStack(item).getMaxStackSize();
			while (count > 0) {
				int size = Math.min(count, maxStack);
				loot.add(new ItemStack(item, size));
				count -= size;
			}
		}
	}

	public record SetCount(Item item, int min, int max) {
		public static final Codec<SetCount> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(SetCount::item),
			Codec.INT.fieldOf("min").forGetter(SetCount::min),
			Codec.INT.fieldOf("max").forGetter(SetCount::max)
		).apply(inst, SetCount::new));
	}

	public record Addition(Item item, float chance, int min, int max) {
		public static final Codec<Addition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Addition::item),
			Codec.FLOAT.fieldOf("chance").forGetter(Addition::chance),
			Codec.INT.optionalFieldOf("min", 1).forGetter(Addition::min),
			Codec.INT.optionalFieldOf("max", 1).forGetter(Addition::max)
		).apply(inst, Addition::new));
	}

	// Every single item of this type survives with keep_chance
	public record Thinning(Item item, float keepChance) {
		public static final Codec<Thinning> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Thinning::item),
			Codec.FLOAT.fieldOf("keep_chance").forGetter(Thinning::keepChance)
		).apply(inst, Thinning::new));
	}
}
