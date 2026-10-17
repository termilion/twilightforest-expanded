package twilightforest.quest;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import twilightforest.entity.passive.QuestRam;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Notifies quest objectives about players interacting with a quest ram: any right click, and a handed in item the ram has finished studying.
 * Objectives are rebuilt whenever quests reload, so the listeners are dropped when the server stops.
 */
public final class QuestRamStudies {
	/**
	 * @param stack    the item in the player's hand, or the studied item
	 * @param handedIn true once the ram has studied {@code stack} and given it back
	 */
	public record Study(ServerPlayer player, QuestRam ram, ItemStack stack, boolean handedIn) {
	}

	private static final List<Consumer<Study>> LISTENERS = new CopyOnWriteArrayList<>();

	public static void addListener(Consumer<Study> listener) {
		LISTENERS.add(listener);
	}

	public static void fireInteraction(ServerPlayer player, QuestRam ram, ItemStack held) {
		fire(new Study(player, ram, held, false));
	}

	public static void fireHandedIn(ServerPlayer player, QuestRam ram, ItemStack studied) {
		fire(new Study(player, ram, studied, true));
	}

	private static void fire(Study study) {
		LISTENERS.forEach(listener -> listener.accept(study));
	}

	public static void clear() {
		LISTENERS.clear();
	}

	private QuestRamStudies() {
	}
}
