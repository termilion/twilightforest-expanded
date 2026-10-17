package twilightforest.quest;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Notifies {@link AllayFreedObjective}s that a player has freed a caged allay.
 * Objectives are rebuilt whenever quests reload, so the listeners are dropped when the server stops.
 */
public final class AllayFreedListeners {
	private static final List<Consumer<ServerPlayer>> LISTENERS = new CopyOnWriteArrayList<>();

	public static void addListener(Consumer<ServerPlayer> listener) {
		LISTENERS.add(listener);
	}

	public static void fire(ServerPlayer player) {
		LISTENERS.forEach(listener -> listener.accept(player));
	}

	public static void clear() {
		LISTENERS.clear();
	}

	private AllayFreedListeners() {
	}
}
