package twilightforest.data.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructures;

import java.util.concurrent.CompletableFuture;

public class StructureTagGenerator extends StructureTagsProvider {
	// Add structures to this tag to show on the Magic Map, detected by worldgen features avoiding landmarks and progression lock behavior
	public static final TagKey<Structure> LANDMARK = TagKey.create(Registries.STRUCTURE, TwilightForestMod.prefix("landmark"));
	// Amethyst straits spikes, blackstone columns, deltas and lakes keep clear of the pieces of these structures
	// Brownies spawn next to these structures
	public static final TagKey<Structure> BROWNIE_HAUNTS = TagKey.create(Registries.STRUCTURE, TwilightForestMod.prefix("brownie_haunts"));
	public static final TagKey<Structure> CLEAR_OF_AMETHYST_FEATURES = TagKey.create(Registries.STRUCTURE, TwilightForestMod.prefix("clear_of_amethyst_features"));

	public StructureTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper helper) {
		super(output, provider, TwilightForestMod.ID, helper);
	}

	@Override
	protected void addTags(HolderLookup.Provider p_256087_) {
		this.tag(LANDMARK).add(
			TFStructures.HEDGE_MAZE,
			TFStructures.QUEST_GROVE,
			TFStructures.MUSHROOM_TOWER,
			TFStructures.HOLLOW_HILL_SMALL,
			TFStructures.HOLLOW_HILL_MEDIUM,
			TFStructures.HOLLOW_HILL_LARGE,
			TFStructures.NAGA_COURTYARD,
			TFStructures.LICH_TOWER,
			TFStructures.LABYRINTH,
			TFStructures.HYDRA_LAIR,
			TFStructures.KNIGHT_STRONGHOLD,
			TFStructures.DARK_TOWER,
			TFStructures.YETI_CAVE,
			TFStructures.AURORA_PALACE,
			TFStructures.TROLL_CAVE
		);

		this.tag(BROWNIE_HAUNTS).add(
			TFStructures.HOLLOW_HILL_SMALL,
			TFStructures.HOLLOW_HILL_MEDIUM,
			TFStructures.HOLLOW_HILL_LARGE,
			TFStructures.HEDGE_MAZE,
			TFStructures.NAGA_COURTYARD,
			TFStructures.QUEST_GROVE,
			TFStructures.CAMP,
			TFStructures.HOLLOW_TREE
		).addOptional(TwilightForestMod.prefix("savanna_gnome_hut"))
			.addOptional(TwilightForestMod.prefix("savanna_quarry"))
			.addOptional(TwilightForestMod.prefix("savanna_stoneworks"))
			.addOptional(TwilightForestMod.prefix("spooky_graveyard"))
			.addOptional(TwilightForestMod.prefix("spooky_wild_ruin"))
			.addOptional(TwilightForestMod.prefix("spooky_firewatch_tower"))
			.addOptional(TwilightForestMod.prefix("spooky_woodland_hut"))
			.addOptional(TwilightForestMod.prefix("spooky_haunted_ruin"))
			.addOptional(TwilightForestMod.prefix("amethyst_ruin_town"));

		// The ruin town is a plain data structure (no TFStructures key)
		this.tag(CLEAR_OF_AMETHYST_FEATURES).addOptional(TwilightForestMod.prefix("amethyst_ruin_town"));
	}
}
