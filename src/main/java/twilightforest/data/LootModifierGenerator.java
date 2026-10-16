package twilightforest.data;

import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFStructures;
import twilightforest.init.TFItems;
import twilightforest.loot.conditions.GiantPickUsedCondition;
import twilightforest.loot.modifiers.ChestLootRulesModifier;
import twilightforest.loot.modifiers.ChestLootRulesModifier.Addition;
import twilightforest.loot.modifiers.ChestLootRulesModifier.Rule;
import twilightforest.loot.modifiers.ChestLootRulesModifier.SetCount;
import twilightforest.loot.modifiers.ChestLootRulesModifier.Thinning;
import twilightforest.loot.modifiers.FieryToolSmeltingModifier;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LootModifierGenerator extends GlobalLootModifierProvider {
	public LootModifierGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider, TwilightForestMod.ID);
	}

	@Override
	protected void start() {
		add("fiery_pick_smelting", new FieryToolSmeltingModifier(new LootItemCondition[]{MatchTool.toolMatches(ItemPredicate.Builder.item().of(TFItems.FIERY_PICKAXE.get())).build()}));
		add("giant_pick_grouping", new GiantToolGroupingModifier(new LootItemCondition[]{GiantPickUsedCondition.builder(LootContext.EntityTarget.THIS).build()}));

		// Container loot in the Twilight Forest, by biome or structure. Applied in this order, so later emerald rules win.
		add("chest_loot", new ChestLootRulesModifier(new LootItemCondition[0], List.of(
			rule("all", List.of(), List.of(), List.of(new Thinning(Items.DIAMOND, 0.7F)), List.of(), List.of(new Addition(Items.GOLD_INGOT, 0.3F, 1, 3))),

			rule("emeralds_forest", List.of(TFBiomes.FOREST, TFBiomes.DENSE_FOREST, TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST), List.of(),
				List.of(), List.of(new SetCount(Items.EMERALD, 0, 4)), List.of()),
			rule("emeralds_mid", List.of(TFBiomes.LAKE, TFBiomes.CLEARING, TFBiomes.OAK_SAVANNAH, TFBiomes.OAK_SAVANNAH_UNDERGROUND, TFBiomes.SNOWY_FOREST, TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER, TFBiomes.SPOOKY_FOREST), List.of(TFStructures.LABYRINTH),
				List.of(), List.of(new SetCount(Items.EMERALD, 1, 6)), List.of()),
			rule("emeralds_high", List.of(TFBiomes.DARKWOOD_CLEARING, TFBiomes.HIGHLANDS, TFBiomes.HIGHLANDS_UNDERGROUND), List.of(TFStructures.LICH_TOWER, TFStructures.AURORA_PALACE),
				List.of(), List.of(new SetCount(Items.EMERALD, 2, 6)), List.of()),
			rule("amethyst_straits", List.of(TFBiomes.AMETHYST_STRAITS, TFBiomes.AMETHYST_STRAITS_UNDERGROUND), List.of(),
				List.of(), List.of(new SetCount(Items.EMERALD, 0, 10)), List.of(new Addition(TFItems.CARMINITE.get(), 0.3F, 1, 3))),

			rule("oak_savannah_gold", List.of(TFBiomes.OAK_SAVANNAH, TFBiomes.OAK_SAVANNAH_UNDERGROUND), List.of(),
				List.of(), List.of(), List.of(new Addition(Items.GOLD_INGOT, 0.7F, 2, 6), new Addition(Items.GOLD_NUGGET, 0.6F, 3, 12))),
			rule("treated_leather", List.of(TFBiomes.DARK_FOREST, TFBiomes.CLEARING), List.of(TFStructures.DARK_TOWER),
				List.of(), List.of(), List.of(new Addition(TFItems.TREATED_LEATHER.get(), 0.25F, 1, 3))),
			rule("forest_trim", List.of(TFBiomes.FOREST, TFBiomes.DENSE_FOREST, TFBiomes.FIREFLY_FOREST, TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.ENCHANTED_FOREST, TFBiomes.SPOOKY_FOREST, TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER, TFBiomes.SNOWY_FOREST), List.of(),
				List.of(), List.of(), List.of(new Addition(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 0.03F, 1, 1))),

			rule("lich_tower", List.of(), List.of(TFStructures.LICH_TOWER),
				List.of(), List.of(), List.of(new Addition(TFItems.CARMINITE.get(), 0.05F, 1, 1))),
			rule("dark_tower", List.of(), List.of(TFStructures.DARK_TOWER),
				List.of(), List.of(), List.of(new Addition(Items.BLAZE_ROD, 0.08F, 1, 2), new Addition(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 0.05F, 1, 1))),
			rule("aurora_palace", List.of(), List.of(TFStructures.AURORA_PALACE),
				List.of(), List.of(), List.of(new Addition(Items.BREEZE_ROD, 0.08F, 1, 2), new Addition(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 0.05F, 1, 1))),
			rule("hedge_maze", List.of(), List.of(TFStructures.HEDGE_MAZE),
				List.of(), List.of(), List.of(new Addition(Items.SNIFFER_EGG, 0.05F, 1, 1))),
			rule("lone_citadel", List.of(), List.of(TFStructures.LONE_CITADEL),
				List.of(), List.of(), List.of(new Addition(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 0.04F, 1, 1), new Addition(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 0.04F, 1, 1)))
		)));
	}


	private static Rule rule(String name, List<ResourceKey<Biome>> biomes, List<ResourceKey<Structure>> structures, List<Thinning> thin, List<SetCount> setCounts, List<Addition> additions) {
		return new Rule(name, biomes.stream().<Either<TagKey<Biome>, ResourceKey<Biome>>>map(Either::right).toList(), structures, thin, setCounts, additions);
	}
}
