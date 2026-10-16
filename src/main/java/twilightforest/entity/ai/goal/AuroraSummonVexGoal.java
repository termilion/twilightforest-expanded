package twilightforest.entity.ai.goal;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.gameevent.GameEvent;
import twilightforest.entity.boss.PrincessAurora;
import twilightforest.init.TFSounds;

import java.util.EnumSet;

/**
 * Channels, then summons a vex. Ported from Feywild (MIT, SaphieNyako) Queen Mab SummonVexGoal.
 */
public class AuroraSummonVexGoal extends Goal {

	private static final int DURATION = 36;
	private static final int VEX_LIFE = 1200;

	private final PrincessAurora entity;
	private int ticksLeft;

	public AuroraSummonVexGoal(PrincessAurora entity) {
		this.entity = entity;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		LivingEntity target = this.entity.getTarget();
		return target != null && target.isAlive()
			&& this.entity.getState() == PrincessAurora.State.IDLE
			&& this.entity.getRandom().nextFloat() < 0.03F;
	}

	@Override
	public void start() {
		this.ticksLeft = DURATION;
		this.entity.setState(PrincessAurora.State.CHANNEL);
	}

	@Override
	public void tick() {
		LivingEntity target = this.entity.getTarget();
		if (target == null || !target.isAlive()) {
			this.ticksLeft = 0;
			return;
		}

		this.ticksLeft--;

		if (this.ticksLeft <= 0) {
			this.summonVex(target);
			return;
		}

		if (this.ticksLeft <= 30) {
			this.entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
			this.entity.getNavigation().moveTo(target, 0.5D);
		}
	}

	private void summonVex(LivingEntity target) {
		Vex vex = EntityType.VEX.create(this.entity.level());
		if (vex == null) return;

		vex.setPos(
			this.entity.getX() + this.entity.getRandom().nextInt(3),
			this.entity.getY() + this.entity.getRandom().nextInt(3),
			this.entity.getZ() + this.entity.getRandom().nextInt(3));
		vex.setOwner(this.entity);
		vex.setLimitedLife(VEX_LIFE);
		vex.setTarget(target);
		vex.setAggressive(true);
		this.entity.level().addFreshEntity(vex);
		this.entity.gameEvent(GameEvent.ENTITY_PLACE, vex);
		this.entity.playSound(TFSounds.PRINCESS_AURORA_SUMMON.get(), 1.0F, 1.0F);
	}

	@Override
	public boolean canContinueToUse() {
		return this.ticksLeft > 0;
	}

	@Override
	public void stop() {
		this.entity.setState(PrincessAurora.State.IDLE);
		this.ticksLeft = 0;
	}
}
