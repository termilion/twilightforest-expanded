package twilightforest.loot.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;
import twilightforest.init.TFDimension;

import java.util.List;

/**
 * Adjusts the loot of containers (chests, barrels, ...) in the Twilight Forest by the biome or structure they sit in.
 * Matches if the container is in one of {@code biomes} or inside a piece of one of {@code structures}; with neither given it matches every container.
 * Order inside one rule: thin, then set_count, then add.
 */
public class ChestLootRuleModifier extends LootModifier {

	// Loot tables that use the chest parameter set but aren't containers
	private static final List<String> EXCLUDED_PATH_PARTS = List.of("entities/", "blocks/", "gameplay/", "archaeology", "spawner", "equipment/", "keys/", "pots/", "dispenser", "rewards/", "items/", "shearing/", "_rack", "dropper", "useless");

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

	public static final MapCodec<ChestLootRuleModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> LootModifier.codecStart(inst)
		.and(ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("biomes", List.of()).forGetter(m -> m.biomes))
		.and(ResourceKey.codec(Registries.STRUCTURE).listOf().optionalFieldOf("structures", List.of()).forGetter(m -> m.structures))
		.and(Thinning.CODEC.listOf().optionalFieldOf("thin", List.of()).forGetter(m -> m.thin))
		.and(SetCount.CODEC.listOf().optionalFieldOf("set_count", List.of()).forGetter(m -> m.setCounts))
		.and(Addition.CODEC.listOf().optionalFieldOf("add", List.of()).forGetter(m -> m.additions))
		.apply(inst, ChestLootRuleModifier::new));

	private final List<ResourceKey<Biome>> biomes;
	private final List<ResourceKey<Structure>> structures;
	private final List<Thinning> thin;
	private final List<SetCount> setCounts;
	private final List<Addition> additions;

	public ChestLootRuleModifier(LootItemCondition[] conditions, List<ResourceKey<Biome>> biomes, List<ResourceKey<Structure>> structures, List<Thinning> thin, List<SetCount> setCounts, List<Addition> additions) {
		super(conditions);
		this.biomes = biomes;
		this.structures = structures;
		this.thin = thin;
		this.setCounts = setCounts;
		this.additions = additions;
	}

	public static boolean isTwilightContainer(LootContext context) {
		ServerLevel level = context.getLevel();
		if (!level.dimension().equals(TFDimension.DIMENSION_KEY)) return false;
		if (!context.hasParam(LootContextParams.ORIGIN) || context.hasParam(LootContextParams.BLOCK_STATE) || context.hasParam(LootContextParams.DAMAGE_SOURCE) || context.hasParam(LootContextParams.TOOL))
			return false;
		ResourceLocation id = context.getQueriedLootTableId();
		String path = id.getPath();
		if (path.endsWith("_pot")) return false;
		for (String part : EXCLUDED_PATH_PARTS) {
			if (path.contains(part)) return false;
		}
		return true;
	}

	private boolean matches(ServerLevel level, BlockPos pos) {
		if (this.biomes.isEmpty() && this.structures.isEmpty()) return true;
		var biome = level.getBiome(pos);
		for (ResourceKey<Biome> key : this.biomes) {
			if (biome.is(key)) return true;
		}
		Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		for (ResourceKey<Structure> key : this.structures) {
			Structure structure = registry.get(key);
			if (structure != null && level.structureManager().getStructureWithPieceAt(pos, structure).isValid()) return true;
		}
		return false;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (!isTwilightContainer(context)) return generatedLoot;
		Vec3 origin = context.getParam(LootContextParams.ORIGIN);
		ServerLevel level = context.getLevel();
		if (!this.matches(level, BlockPos.containing(origin))) return generatedLoot;
		RandomSource random = context.getRandom();

		for (Thinning thinning : this.thin) {
			for (ItemStack stack : generatedLoot) {
				if (!stack.is(thinning.item())) continue;
				int kept = 0;
				for (int i = 0; i < stack.getCount(); i++) {
					if (random.nextFloat() < thinning.keepChance()) kept++;
				}
				stack.setCount(kept);
			}
		}

		for (SetCount setCount : this.setCounts) {
			generatedLoot.forEach(stack -> {
				if (stack.is(setCount.item())) stack.setCount(0);
			});
			addStacks(generatedLoot, setCount.item(), random.nextIntBetweenInclusive(setCount.min(), setCount.max()));
		}

		for (Addition addition : this.additions) {
			if (random.nextFloat() < addition.chance())
				addStacks(generatedLoot, addition.item(), random.nextIntBetweenInclusive(addition.min(), addition.max()));
		}

		generatedLoot.removeIf(ItemStack::isEmpty);
		return generatedLoot;
	}

	private static void addStacks(ObjectArrayList<ItemStack> loot, Item item, int count) {
		int maxStack = new ItemStack(item).getMaxStackSize();
		while (count > 0) {
			int size = Math.min(count, maxStack);
			loot.add(new ItemStack(item, size));
			count -= size;
		}
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
