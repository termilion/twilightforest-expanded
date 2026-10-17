package twilightforest.entity.ai.goal.ashenlord;

import twilightforest.entity.boss.AshenLord;
import twilightforest.init.TFSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class AshenLordRetaliationGoal extends Goal {

	private static final int RETALIATION_COOLDOWN = 20 * 6;
	private static final float AGITATION_RESET_CHANCE = 0.25F;

	private final AshenLord entity;

	private int cooldownTicks;

	public AshenLordRetaliationGoal(AshenLord entity) {
		this.entity = entity;
	}

	@Override
	public boolean canUse() {
		if (cooldownTicks > 0) {
			cooldownTicks--;
			return false;
		}

		if (!entity.hasRetaliationRequest()) {
			return false;
		}

		LivingEntity target = entity.getTarget();

		return target != null
				&& target.isAlive()
				&& entity.getState() != AshenLord.State.CHANNEL;
	}

	@Override
	public void start() {
		AshenLord.ForcedAbility[] abilities = AshenLord.ForcedAbility.values();
		AshenLord.ForcedAbility selected = abilities[entity.getRandom().nextInt(abilities.length)];

		entity.setForcedAbility(selected);
		entity.consumeRetaliationRequest();

		cooldownTicks = RETALIATION_COOLDOWN;
		playRetaliationVoice();
	}

	private void playRetaliationVoice() {
		if (entity.getAgitationStage() == 0) {

			entity.playSound(
					TFSounds.ASHEN_LORD_CHANNEL_01.get(),
					2F,
					1.0F
			);

			entity.increaseAgitationStage();

		} else if (entity.getAgitationStage() == 1) {

			entity.playSound(
					TFSounds.ASHEN_LORD_CHANNEL_02.get(),
					2F,
					1.0F
			);

			entity.increaseAgitationStage();

		} else {

			entity.playSound(
					TFSounds.ASHEN_LORD_ATTACK.get(),
					2F,
					1.0F
			);

			if (entity.getRandom().nextFloat() < AGITATION_RESET_CHANCE) {
				entity.resetAgitationStage();
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}
}