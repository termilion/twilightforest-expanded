package twilightforest.quest;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.questlog.core.quests.objectives.entity.AbstractEntityObjective;
import org.infernalstudios.questlog.util.ItemMatcher;

/**
 * {@code twilightforest:entity_interact}: counts a player's interaction with a quest ram.
 * {@code entity} (optional) filters the ram. Without {@code item} any right click on the ram counts.
 * With {@code item} (id or tag, plus optional {@code components}) it only counts once the ram has studied a matching item
 * the player handed in and given it back.
 */
public class EntityInteractObjective extends AbstractEntityObjective {
	private final ItemMatcher itemMatcher;
	private final boolean needsItem;

	public EntityInteractObjective(JsonObject definition) {
		super(definition);
		this.itemMatcher = ItemMatcher.fromDefinition(definition);
		this.needsItem = definition.has("item");
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();
		QuestRamStudies.addListener(this::onStudied);
	}

	private void onStudied(QuestRamStudies.Study study) {
		if (this.isCompleted() || this.getParent() == null || this.getParent().manager == null) return;
		if (!study.player().equals(this.getParent().manager.player)) return;
		if (!this.test((Entity) study.ram())) return;
		if (this.needsItem) {
			ItemStack stack = study.stack();
			if (!study.handedIn() || !this.itemMatcher.test(stack, study.player().level().registryAccess())) return;
		}
		this.setUnits(this.getUnits() + 1);
	}
}
