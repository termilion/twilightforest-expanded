package twilightforest.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import twilightforest.init.TFAdvancements;

import java.util.Optional;

/**
 * Fired when a player completes a Questlog quest. Progression advancements use it to hand out their keys.
 */
public class QuestCompletedTrigger extends SimpleCriterionTrigger<QuestCompletedTrigger.TriggerInstance> {

	@Override
	public Codec<QuestCompletedTrigger.TriggerInstance> codec() {
		return QuestCompletedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, ResourceLocation quest) {
		this.trigger(player, (instance) -> instance.test(quest));
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation quest) implements SimpleInstance {

		public static final Codec<QuestCompletedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(QuestCompletedTrigger.TriggerInstance::player),
				ResourceLocation.CODEC.fieldOf("quest").forGetter(QuestCompletedTrigger.TriggerInstance::quest))
			.apply(instance, QuestCompletedTrigger.TriggerInstance::new));

		public static Criterion<QuestCompletedTrigger.TriggerInstance> completedQuest(ResourceLocation quest) {
			return TFAdvancements.QUEST_COMPLETED.get().createCriterion(new QuestCompletedTrigger.TriggerInstance(Optional.empty(), quest));
		}

		boolean test(ResourceLocation quest) {
			return this.quest.equals(quest);
		}
	}
}
