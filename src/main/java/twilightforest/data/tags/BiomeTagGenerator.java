package twilightforest.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;

import java.util.concurrent.CompletableFuture;

public class BiomeTagGenerator extends BiomeTagsProvider {

	public static final TagKey<Biome> IS_TWILIGHT = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("in_twilight_forest"));

	public static final TagKey<Biome> VALID_BROWNIE_HUT_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_brownie_hut_biomes"));
	public static final TagKey<Biome> VALID_BROWNIE_HOLE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_brownie_hole_biomes"));
	public static final TagKey<Biome> VALID_ELVEN_PILLAR_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_elven_pillar_biomes"));
	public static final TagKey<Biome> VALID_SNOWY_RUIN_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_snowy_ruin_biomes"));
	public static final TagKey<Biome> VALID_ELVEN_TREE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_elven_tree_biomes"));
	public static final TagKey<Biome> VALID_REDCAP_FORT_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_redcap_fort_biomes"));
	public static final TagKey<Biome> VALID_UNDERGROUND_HOUSE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_underground_house_biomes"));
	public static final TagKey<Biome> VALID_QUEST_GROVE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_quest_grove_biomes"));

	public static final TagKey<Biome> VALID_HOLLOW_TREE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_hollow_tree_biomes"));
	public static final TagKey<Biome> VALID_HEDGE_MAZE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_hedge_maze_biomes"));
	public static final TagKey<Biome> VALID_HOLLOW_HILL_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_hollow_hill_biomes"));
	public static final TagKey<Biome> VALID_CAMP_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_camp_biomes"));
	public static final TagKey<Biome> VALID_MUSHROOM_TOWER_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_mushroom_tower_biomes"));

	public static final TagKey<Biome> VALID_NAGA_COURTYARD_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_naga_courtyard_biomes"));
	public static final TagKey<Biome> VALID_LICH_TOWER_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_lich_tower_biomes"));
	public static final TagKey<Biome> VALID_LABYRINTH_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_labyrinth_biomes"));
	public static final TagKey<Biome> VALID_HYDRA_LAIR_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_hydra_lair_biomes"));
	public static final TagKey<Biome> VALID_KNIGHT_STRONGHOLD_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_knight_stronghold_biomes"));
	public static final TagKey<Biome> VALID_DARK_TOWER_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_dark_tower_biomes"));
	public static final TagKey<Biome> VALID_YETI_CAVE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_yeti_cave_biomes"));
	public static final TagKey<Biome> VALID_AURORA_PALACE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_aurora_palace_biomes"));
	public static final TagKey<Biome> VALID_TROLL_CAVE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_troll_cave_biomes"));
	public static final TagKey<Biome> VALID_GIANT_HOUSE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_giant_house_biomes"));
	public static final TagKey<Biome> VALID_UNDEAD_CRYPT_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_undead_crypt_biomes"));
	public static final TagKey<Biome> VALID_LONE_CITADEL_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_lone_citadel_biomes"));
	public static final TagKey<Biome> VALID_SPOOKY_RUIN_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_spooky_ruin_biomes"));
	public static final TagKey<Biome> VALID_SAVANNA_LANDMARK_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_savanna_landmark_biomes"));
	public static final TagKey<Biome> VALID_FINAL_CASTLE_BIOMES = TagKey.create(Registries.BIOME, TwilightForestMod.prefix("valid_final_castle_biomes"));

	public BiomeTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper helper) {
		super(output, provider, TwilightForestMod.ID, helper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {

		this.tag(IS_TWILIGHT).add(
			TFBiomes.CLEARING, TFBiomes.DENSE_FOREST,
			TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.FIREFLY_FOREST,
			TFBiomes.FOREST, TFBiomes.MUSHROOM_FOREST,
			TFBiomes.OAK_SAVANNAH, TFBiomes.SPOOKY_FOREST,
			TFBiomes.ENCHANTED_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST,
			TFBiomes.LAKE, TFBiomes.STREAM, TFBiomes.UNDERGROUND,
			TFBiomes.SWAMP, TFBiomes.FIRE_SWAMP,
			TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER,
			TFBiomes.SNOWY_FOREST, TFBiomes.GLACIER,
			TFBiomes.HIGHLANDS, TFBiomes.THORNLANDS,
			TFBiomes.AMETHYST_STRAITS, TFBiomes.AMETHYST_STRAITS_UNDERGROUND,
			TFBiomes.OAK_SAVANNAH_UNDERGROUND
		);

		this.tag(VALID_QUEST_GROVE_BIOMES).add(TFBiomes.ENCHANTED_FOREST);

		// Trek / Dungeons and Taverns / vanilla imports (structures in data/twilightforest/worldgen/structure, types only generate in the Twilight Forest)
		this.tag(VALID_BROWNIE_HUT_BIOMES).add(TFBiomes.SWAMP, TFBiomes.FIRE_SWAMP, Biomes.MANGROVE_SWAMP);
		this.tag(VALID_BROWNIE_HOLE_BIOMES).add(TFBiomes.FOREST, TFBiomes.DENSE_FOREST, TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.HIGHLANDS, TFBiomes.SNOWY_FOREST, TFBiomes.OAK_SAVANNAH);
		this.tag(VALID_REDCAP_FORT_BIOMES).add(TFBiomes.OAK_SAVANNAH);
		// Every surface biome but stream and lake
		this.tag(VALID_ELVEN_PILLAR_BIOMES).add(
			TFBiomes.FOREST, TFBiomes.DENSE_FOREST, TFBiomes.FIREFLY_FOREST, TFBiomes.CLEARING, TFBiomes.OAK_SAVANNAH,
			TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.ENCHANTED_FOREST, TFBiomes.SPOOKY_FOREST,
			TFBiomes.SWAMP, TFBiomes.FIRE_SWAMP, Biomes.MANGROVE_SWAMP, TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER,
			TFBiomes.SNOWY_FOREST, TFBiomes.GLACIER, TFBiomes.HIGHLANDS, TFBiomes.THORNLANDS, TFBiomes.AMETHYST_STRAITS
		);
		this.tag(VALID_SNOWY_RUIN_BIOMES).add(TFBiomes.SNOWY_FOREST);
		this.tag(VALID_ELVEN_TREE_BIOMES).add(TFBiomes.FOREST, TFBiomes.DENSE_FOREST);
		this.tag(VALID_UNDERGROUND_HOUSE_BIOMES).add(TFBiomes.UNDERGROUND);
		this.tag(VALID_MUSHROOM_TOWER_BIOMES).add(TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST);

		this.tag(VALID_CAMP_BIOMES).add(
			TFBiomes.CLEARING, TFBiomes.MUSHROOM_FOREST, TFBiomes.FOREST, TFBiomes.FIREFLY_FOREST
		);

		this.tag(VALID_HOLLOW_TREE_BIOMES).add(
			TFBiomes.DENSE_FOREST, TFBiomes.FIRE_SWAMP,
			TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.FIREFLY_FOREST,
			TFBiomes.FOREST, TFBiomes.MUSHROOM_FOREST,
			TFBiomes.ENCHANTED_FOREST
		);
		this.tag(VALID_HEDGE_MAZE_BIOMES).add(
			TFBiomes.CLEARING, TFBiomes.DENSE_FOREST,
			TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.FIREFLY_FOREST,
			TFBiomes.FOREST, TFBiomes.MUSHROOM_FOREST
		);
		this.tag(VALID_HOLLOW_HILL_BIOMES).add(
			TFBiomes.CLEARING, TFBiomes.DENSE_FOREST,
			TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.FIREFLY_FOREST,
			TFBiomes.FOREST, TFBiomes.MUSHROOM_FOREST
		);
		this.tag(VALID_NAGA_COURTYARD_BIOMES).add(
			TFBiomes.CLEARING, TFBiomes.DENSE_FOREST,
			TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.FIREFLY_FOREST,
			TFBiomes.FOREST, TFBiomes.MUSHROOM_FOREST
		);
		this.tag(VALID_LICH_TOWER_BIOMES).add(Biomes.MANGROVE_SWAMP);
		this.tag(VALID_LABYRINTH_BIOMES).add(TFBiomes.SWAMP);
		this.tag(VALID_HYDRA_LAIR_BIOMES).add(TFBiomes.FIRE_SWAMP);
		this.tag(VALID_KNIGHT_STRONGHOLD_BIOMES).add(TFBiomes.DARK_FOREST);
		this.tag(VALID_DARK_TOWER_BIOMES).add(TFBiomes.DARK_FOREST_CENTER);
		this.tag(VALID_YETI_CAVE_BIOMES).add(TFBiomes.SNOWY_FOREST);
		this.tag(VALID_AURORA_PALACE_BIOMES).add(TFBiomes.GLACIER);
		this.tag(VALID_TROLL_CAVE_BIOMES).add(TFBiomes.HIGHLANDS);
		this.tag(VALID_UNDEAD_CRYPT_BIOMES).add(TFBiomes.SPOOKY_FOREST);
		this.tag(VALID_LONE_CITADEL_BIOMES).add(TFBiomes.AMETHYST_STRAITS);
		this.tag(VALID_SPOOKY_RUIN_BIOMES).add(TFBiomes.SPOOKY_FOREST);
		this.tag(VALID_SAVANNA_LANDMARK_BIOMES).add(TFBiomes.OAK_SAVANNAH);
		this.tag(VALID_GIANT_HOUSE_BIOMES).add(TFBiomes.HIGHLANDS);

		//apparently using forge and vanilla tags allows other mods to spawn stuff in our biomes. Will keep these commented out here just in case we need to reference them later.
		//vanilla biome categories
//		this.tag(BiomeTags.IS_FOREST).add(
//				TFBiomes.FOREST, TFBiomes.DENSE_FOREST, TFBiomes.FIREFLY_FOREST,
//				TFBiomes.OAK_SAVANNAH, TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST,
//				TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER,
//				TFBiomes.SNOWY_FOREST, TFBiomes.HIGHLANDS
//		);
//		this.tag(BiomeTags.IS_MOUNTAIN).add(TFBiomes.HIGHLANDS);
//		this.tag(BiomeTags.IS_HILL).add(TFBiomes.THORNLANDS);

		//forge biome categories
//		this.tag(Tags.Biomes.IS_DENSE).add(TFBiomes.DENSE_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER, TFBiomes.SNOWY_FOREST, TFBiomes.THORNLANDS);
//		this.tag(Tags.Biomes.IS_SPARSE).add(TFBiomes.CLEARING, TFBiomes.OAK_SAVANNAH, TFBiomes.GLACIER, TFBiomes.FINAL_PLATEAU);
//		this.tag(Tags.Biomes.IS_PLAINS).add(TFBiomes.CLEARING);
//		this.tag(Tags.Biomes.IS_MUSHROOM).add(TFBiomes.MUSHROOM_FOREST, TFBiomes.DENSE_MUSHROOM_FOREST);
//		this.tag(Tags.Biomes.IS_RARE).add(TFBiomes.ENCHANTED_FOREST, TFBiomes.SPOOKY_FOREST, TFBiomes.CLEARING, TFBiomes.DENSE_MUSHROOM_FOREST, TFBiomes.LAKE);
//		this.tag(Tags.Biomes.IS_WATER).add(TFBiomes.LAKE, TFBiomes.STREAM);
//		this.tag(Tags.Biomes.IS_MAGICAL).add(TFBiomes.ENCHANTED_FOREST, TFBiomes.DARK_FOREST_CENTER);
//		this.tag(Tags.Biomes.IS_SPOOKY).add(TFBiomes.SPOOKY_FOREST, TFBiomes.DARK_FOREST, TFBiomes.DARK_FOREST_CENTER);
//		this.tag(Tags.Biomes.IS_DEAD).add(TFBiomes.SPOOKY_FOREST, TFBiomes.THORNLANDS, TFBiomes.FINAL_PLATEAU);
//		this.tag(Tags.Biomes.IS_SWAMP).add(TFBiomes.SWAMP, TFBiomes.FIRE_SWAMP);
//		this.tag(Tags.Biomes.IS_SNOWY).add(TFBiomes.SNOWY_FOREST);
//		this.tag(Tags.Biomes.IS_CONIFEROUS).add(TFBiomes.SNOWY_FOREST, TFBiomes.HIGHLANDS);
//		this.tag(Tags.Biomes.IS_COLD).add(TFBiomes.SNOWY_FOREST, TFBiomes.GLACIER);
//		this.tag(Tags.Biomes.IS_WASTELAND).add(TFBiomes.GLACIER, TFBiomes.THORNLANDS, TFBiomes.FINAL_PLATEAU);
//		this.tag(Tags.Biomes.IS_DRY).add(TFBiomes.THORNLANDS, TFBiomes.FINAL_PLATEAU);
//		this.tag(Tags.Biomes.IS_PLATEAU).add(TFBiomes.FINAL_PLATEAU);
//		this.tag(Tags.Biomes.IS_UNDERGROUND).add(TFBiomes.UNDERGROUND);

		//other vanilla tags
		this.tag(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS).addTag(IS_TWILIGHT);
		this.tag(BiomeTags.WITHOUT_PATROL_SPAWNS).addTag(IS_TWILIGHT);
		this.tag(BiomeTags.WITHOUT_ZOMBIE_SIEGES).addTag(IS_TWILIGHT);

		//even though we won't spawn vanilla frogs, we'll still add support for the variants
		this.tag(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS).add(TFBiomes.LAKE);
		// Vanilla mineshafts; the start is checked at its final (underground) position, the surface biome covers a start that ends up higher
		this.tag(BiomeTags.HAS_MINESHAFT).add(TFBiomes.OAK_SAVANNAH, TFBiomes.OAK_SAVANNAH_UNDERGROUND);
		this.tag(BiomeTags.HAS_RUINED_PORTAL_STANDARD).add(TFBiomes.DARK_FOREST_CENTER);
		this.tag(BiomeTags.SPAWNS_COLD_VARIANT_FROGS).add(TFBiomes.SNOWY_FOREST, TFBiomes.GLACIER);
		this.tag(BiomeTags.SPAWNS_WARM_VARIANT_FROGS).add(TFBiomes.OAK_SAVANNAH, TFBiomes.FIRE_SWAMP);

		this.tag(BiomeTags.SPAWNS_SNOW_FOXES).add(TFBiomes.SNOWY_FOREST, TFBiomes.GLACIER);
		this.tag(BiomeTags.SPAWNS_WHITE_RABBITS).add(TFBiomes.SNOWY_FOREST, TFBiomes.GLACIER);
		this.tag(BiomeTags.SNOW_GOLEM_MELTS).add(TFBiomes.OAK_SAVANNAH, TFBiomes.FIRE_SWAMP);

		this.tag(BiomeTags.HAS_CLOSER_WATER_FOG).add(TFBiomes.SPOOKY_FOREST, TFBiomes.SWAMP, TFBiomes.FIRE_SWAMP);
	}

	@Override
	public String getName() {
		return "Twilight Forest Biome Tags";
	}
}