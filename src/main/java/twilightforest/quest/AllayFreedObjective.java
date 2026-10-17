package twilightforest.quest;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.objectives.Objective;

/**
 * {@code twilightforest:allay_freed}: completes when the player has spoken to a caged allay of a pillager outpost or woodland mansion, see {@link CagedAllay}.
 */
public class AllayFreedObjective extends Objective {
	public AllayFreedObjective(JsonObject definition) {
		super(definition);
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();
		AllayFreedListeners.addListener(this::onFreed);
	}

	private void onFreed(ServerPlayer player) {
		if (this.isCompleted() || this.getParent() == null || this.getParent().manager == null) return;
		if (!player.equals(this.getParent().manager.player)) return;
		this.setUnits(this.getRequiredAmount());
	}
}
