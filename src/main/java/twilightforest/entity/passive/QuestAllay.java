package twilightforest.entity.passive;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


/**
 * A purple allay of the enchanted forest. Like the vanilla allay it never despawns, and like a dolphin it blesses
 * nearby players: Regeneration I and Speed I for 30 seconds, at most once every 2 minutes per player. While a player is blessed,
 * quest allays around them fly circles around that player.
 */
public class QuestAllay extends Allay {
	private static final double BUFF_RANGE = 6.0D;
	private static final double ORBIT_RANGE = 24.0D;
	private static final int BUFF_DURATION = 30 * 20;
	private static final int BUFF_COOLDOWN = 2 * 60 * 20;
	// Game times, stored in the player's persistent data
	private static final String BUFF_UNTIL = "twilightforest:quest_allay_buff_until";
	private static final String COOLDOWN_UNTIL = "twilightforest:quest_allay_cooldown_until";

	public QuestAllay(EntityType<? extends QuestAllay> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (!(this.level() instanceof ServerLevel level))
			return;

		long time = level.getGameTime();
		if (this.tickCount % 10 == 0)
			for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(BUFF_RANGE), this::canBless))
				this.bless(level, player, time);

		Player blessed = this.findBlessedPlayer(level, time);
		if (blessed != null)
			this.circle(blessed);
	}

	private boolean canBless(Player player) {
		return player.isAlive() && !player.isSpectator() && player.getPersistentData().getLong(COOLDOWN_UNTIL) <= this.level().getGameTime();
	}

	private void bless(ServerLevel level, Player player, long time) {
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF_DURATION, 0), this);
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, BUFF_DURATION, 0), this);
		CompoundTag data = player.getPersistentData();
		data.putLong(BUFF_UNTIL, time + BUFF_DURATION);
		data.putLong(COOLDOWN_UNTIL, time + BUFF_COOLDOWN);
		level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + player.getBbHeight() + 0.3D, player.getZ(), 5, 0.4D, 0.2D, 0.4D, 0.0D);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.2F);
	}

	@Nullable
	private Player findBlessedPlayer(ServerLevel level, long time) {
		return level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), ORBIT_RANGE, entity ->
			entity instanceof Player player && player.isAlive() && !player.isSpectator() && player.getPersistentData().getLong(BUFF_UNTIL) > time);
	}

	// Fly a circle around the player; each allay gets its own phase, so several of them spread out around the player
	private void circle(Player player) {
		double angle = this.tickCount * 0.12D + this.getId() * 2.4D;
		double radius = 2.5D;
		Vec3 target = player.position().add(Mth.cos((float) angle) * radius, 1.2D + Mth.sin(this.tickCount * 0.07F + this.getId()) * 0.6D, Mth.sin((float) angle) * radius);

		// The brain and navigation would steer elsewhere; the move control runs after this step and takes the circle target instead
		this.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		this.getNavigation().stop();
		this.getMoveControl().setWantedPosition(target.x, target.y, target.z, 2.0D);
		this.getLookControl().setLookAt(player);
	}
}
