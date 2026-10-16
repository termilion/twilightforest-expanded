package twilightforest.entity.ai.goal;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import twilightforest.entity.boss.PrincessAurora;
import twilightforest.init.TFSounds;

import java.util.EnumSet;

/**
 * Freezes and slows the target. Ported from Feywild (MIT, SaphieNyako) Queen Mab IntimidateGoal.
 */
public class AuroraIntimidateGoal extends Goal {

	private static final int DURATION = 36;
	private static final int EFFECT_TICKS = 180;

	private final PrincessAurora entity;
	private int ticksLeft;

	public AuroraIntimidateGoal(PrincessAurora entity) {
		this.entity = entity;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = this.entity.getTarget();
		return target != null && target.isAlive()
			&& this.entity.getState() == PrincessAurora.State.IDLE
			&& this.entity.getRandom().nextFloat() < 0.04F;
	}

	@Override
	public void start() {
		this.ticksLeft = DURATION;
		this.entity.setState(PrincessAurora.State.INTIMIDATION);
	}

	@Override
	public void tick() {
		LivingEntity target = this.entity.getTarget();
		if (target == null || !target.isAlive()) {
			this.ticksLeft = 0;
			return;
		}

		this.ticksLeft--;
		this.entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());

		if (this.ticksLeft == 30) {
			this.entity.playSound(TFSounds.PRINCESS_AURORA_INTIMIDATE.get(), 1.0F, 1.0F);
			if (target instanceof Player player) {
				player.setTicksFrozen(EFFECT_TICKS);
			}
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_TICKS, 3), this.entity);
		}
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity target = this.entity.getTarget();
		return this.ticksLeft > 0 && target != null && target.isAlive();
	}

	@Override
	public void stop() {
		this.entity.setState(PrincessAurora.State.IDLE);
		this.ticksLeft = 0;
	}
}
