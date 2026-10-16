package twilightforest.entity.ai.goal;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.boss.PrincessAurora;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;

import java.util.EnumSet;

/**
 * Teleports behind the target and slashes. Ported from Feywild (MIT, SaphieNyako) Queen Mab PhysicalAttackGoal.
 */
public class AuroraStrikeGoal extends Goal {

	private static final int ATTACK_DURATION = 36;
	private static final int MIN_COOLDOWN = 20 * 4;
	private static final int EXTRA_COOLDOWN = 20 * 3;
	private static final int RETALIATION_COOLDOWN = 8;

	private final PrincessAurora entity;
	private int ticksLeft;
	private int cooldownTicks;

	public AuroraStrikeGoal(PrincessAurora entity) {
		this.entity = entity;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		Player attacker = this.entity.getRecentAttacker();
		boolean retaliating = attacker != null && this.entity.distanceToSqr(attacker) <= 24.0D * 24.0D;

		if (this.cooldownTicks > 0) {
			if (retaliating) {
				this.cooldownTicks = Math.min(this.cooldownTicks, RETALIATION_COOLDOWN);
			}
			this.cooldownTicks--;
			return false;
		}

		LivingEntity target = retaliating ? attacker : this.entity.getTarget();
		if (target == null || !target.isAlive() || this.entity.getState() != PrincessAurora.State.IDLE) {
			return false;
		}

		return retaliating || this.entity.getRandom().nextFloat() < 0.06F;
	}

	@Override
	public void start() {
		this.ticksLeft = ATTACK_DURATION;
		this.entity.getNavigation().stop();
		this.entity.setState(PrincessAurora.State.ATTACKING);
	}

	@Override
	public void tick() {
		LivingEntity target = this.getAttackTarget();
		if (target == null || !target.isAlive()) {
			this.ticksLeft = 0;
			return;
		}

		this.entity.getNavigation().stop();
		this.entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());

		if (this.ticksLeft == 35) {
			this.teleportBehind(target);
		}

		if (this.ticksLeft == 33) {
			this.entity.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
			if (this.entity.getRandom().nextInt(3) == 0) {
				this.entity.playSound(TFSounds.PRINCESS_AURORA_ATTACK.get(), 1.0F, 1.0F);
			}
		}

		if (this.ticksLeft == 30) {
			this.performAttack(target);
		}

		this.ticksLeft--;
	}

	@Nullable
	private LivingEntity getAttackTarget() {
		Player attacker = this.entity.getRecentAttacker();
		return attacker != null ? attacker : this.entity.getTarget();
	}

	private void performAttack(LivingEntity target) {
		if (this.entity.distanceToSqr(target) > 4.0D * 4.0D || !this.entity.doHurtTarget(target)) {
			return;
		}

		Vec3 direction = target.position().subtract(this.entity.position());
		direction = new Vec3(direction.x(), 0.0D, direction.z());

		if (direction.lengthSqr() > 1.0E-4D) {
			direction = direction.normalize();
			target.push(direction.x() * 0.8D, 0.25D, direction.z() * 0.8D);
			target.hurtMarked = true;
		}
	}

	private void teleportBehind(LivingEntity target) {
		if (!(this.entity.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Vec3 behind = target.position().subtract(target.getLookAngle().scale(2.0D));
		// the palace is small: never teleport into walls or ceiling
		if (!serverLevel.noCollision(this.entity, this.entity.getBoundingBox().move(behind.subtract(this.entity.position())))) {
			return;
		}

		this.spawnTeleportEffect(serverLevel, this.entity.getX(), this.entity.getY(), this.entity.getZ());
		this.entity.teleportTo(behind.x(), behind.y(), behind.z());
		this.spawnTeleportEffect(serverLevel, behind.x(), behind.y(), behind.z());
		this.entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
	}

	private void spawnTeleportEffect(ServerLevel level, double x, double y, double z) {
		double height = y + this.entity.getBbHeight() * 0.6D;
		level.sendParticles(ParticleTypes.SNOWFLAKE, x, height, z, 24, 0.5D, 0.7D, 0.5D, 0.01D);
		level.sendParticles(TFParticleType.SNOW.get(), x, height, z, 18, 0.45D, 0.45D, 0.45D, 0.02D);
	}

	@Override
	public boolean canContinueToUse() {
		return this.ticksLeft > 0 && this.entity.isAlive();
	}

	@Override
	public boolean isInterruptable() {
		return false;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void stop() {
		this.entity.setState(PrincessAurora.State.IDLE);
		this.ticksLeft = 0;
		this.cooldownTicks = MIN_COOLDOWN + this.entity.getRandom().nextInt(EXTRA_COOLDOWN + 1);
	}
}
