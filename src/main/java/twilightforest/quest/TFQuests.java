package twilightforest.quest;

import net.minecraft.resources.ResourceLocation;

/**
 * Ids of the Questlog quests this mod ships. Questlog derives a quest id from its file below {@code config/questlog/quests/},
 * always in the {@code questlog} namespace; see {@link TFQuestlog#installQuestFiles()}.
 */
public final class TFQuests {
	public static final String FOLDER = "twilightforest";
	public static final ResourceLocation CHAPTER = quest("");

	public static final ResourceLocation CAGED_ALLAY = quest("caged_allay");
	public static final ResourceLocation INTO_THE_TWILIGHT = quest("into_the_twilight");
	public static final ResourceLocation FIND_THE_RAM = quest("find_the_ram");
	public static final ResourceLocation LICH = quest("lich");
	public static final ResourceLocation HYDRA = quest("hydra");
	public static final ResourceLocation UR_GHAST = quest("ur_ghast");
	public static final ResourceLocation AURORA = quest("aurora");
	public static final ResourceLocation PALE_MONARCH = quest("pale_monarch");
	public static final ResourceLocation ASHEN_LORD = quest("ashen_lord");

	private static ResourceLocation quest(String name) {
		return ResourceLocation.fromNamespaceAndPath("questlog", name.isEmpty() ? FOLDER : FOLDER + "/" + name);
	}

	private TFQuests() {
	}
}
