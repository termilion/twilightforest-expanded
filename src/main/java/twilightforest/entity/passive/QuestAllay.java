package twilightforest.entity.passive;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import twilightforest.entity.passive.dialogue.DialogueManager;
import twilightforest.quest.AllayIntro;

import java.util.UUID;


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

	private static final String DIALOGUE_MOB = "quest_allay";
	// The intro: fly around the new player, say three lines, trigger the first quest, spin and vanish
	private static final double INTRO_RADIUS = 2.0D;
	private static final double INTRO_SPAWN_RADIUS = 3.0D;
	private static final double INTRO_SPEED = Math.PI / 30.0D; // half a circle in 30 ticks: from behind the player to in front of them
	private static final int[] INTRO_LINE_TICKS = {40, 110, 180};
	private static final int INTRO_SPIN_START = 250;
	private static final int INTRO_SPIN_TICKS = 40;
	private static final int INTRO_SPINS = 3;

	@Nullable
	private UUID introPlayer;
	private int introTick;
	private double introAngle;

	public QuestAllay(EntityType<? extends QuestAllay> type, Level level) {
		super(type, level);
	}

	public boolean isIntroFor(Player player) {
		return player.getUUID().equals(this.introPlayer);
	}

	/**
	 * Makes this allay greet a player entering the twilight forest for the first time, see {@link AllayIntro}.
	 * Call before the allay is added to the level.
	 */
	public void startIntro(Player player) {
		this.introPlayer = player.getUUID();
		this.introTick = 0;
		// Start behind the player
		this.introAngle = Math.atan2(player.getLookAngle().z, player.getLookAngle().x) + Math.PI;
		this.setInvulnerable(true);
		this.setPos(this.introPosition(player, INTRO_SPAWN_RADIUS, 0.0D));
	}

	// An intro allay is not worth keeping when the server stops half way
	@Override
	public boolean shouldBeSaved() {
		return this.introPlayer == null && super.shouldBeSaved();
	}

	private Vec3 introPosition(Player player, double radius, double lift) {
		return player.position().add(Math.cos(this.introAngle) * radius, player.getEyeHeight() + 0.2D + lift, Math.sin(this.introAngle) * radius);
	}

	private void introStep(ServerLevel level) {
		Player player = this.introPlayer == null ? null : level.getPlayerByUUID(this.introPlayer);
		if (player == null || !player.isAlive() || player.level() != level) {
			this.discard();
			return;
		}

		int tick = this.introTick++;
		if (tick < INTRO_SPIN_START) {
			this.introAngle += INTRO_SPEED;
			this.followIntroPosition(player, this.introPosition(player, INTRO_RADIUS, 0.0D));
			for (int i = 0; i < INTRO_LINE_TICKS.length; i++) {
				if (tick == INTRO_LINE_TICKS[i]) {
					this.say(player, "intro_" + (i + 1));
				}
			}
		} else if (tick < INTRO_SPIN_START + INTRO_SPIN_TICKS) {
			if (tick == INTRO_SPIN_START && player instanceof ServerPlayer serverPlayer) {
				AllayIntro.triggerFirstQuest(serverPlayer);
			}
			// Accelerate into the spins and rise a little
			float progress = (tick - INTRO_SPIN_START + 1) / (float) INTRO_SPIN_TICKS;
			double spinSpeed = INTRO_SPINS * 2.0D * Math.PI / INTRO_SPIN_TICKS * 2.0D * progress;
			this.introAngle += spinSpeed;
			this.followIntroPosition(player, this.introPosition(player, INTRO_RADIUS, progress * 0.6D));
		} else {
			this.vanish(level);
		}
	}

	private void followIntroPosition(Player player, Vec3 target) {
		this.setDeltaMovement(Vec3.ZERO);
		this.setPos(target);
		this.getLookControl().setLookAt(player);
	}

	private void say(Player player, String key) {
		String line = DialogueManager.INSTANCE.pickLine(player, DIALOGUE_MOB, key, this.getRandom(), null);
		if (line != null) {
			DialogueManager.INSTANCE.say(player, DIALOGUE_MOB, line, this.getRandom());
			this.playSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 1.0F, 1.0F + this.getRandom().nextFloat() * 0.4F);
		}
	}

	private void vanish(ServerLevel level) {
		level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.71F, 0.44F, 0.88F), this.getX(), this.getY() + 0.3D, this.getZ(), 60, 0.4D, 0.4D, 0.4D, 1.0D);
		level.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.3D, this.getZ(), 30, 0.4D, 0.4D, 0.4D, 0.1D);
		level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.3D, this.getZ(), 15, 0.3D, 0.3D, 0.3D, 0.05D);
		level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.4F);
		this.discard();
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected void customServerAiStep() {
		if (this.introPlayer != null && this.level() instanceof ServerLevel introLevel) {
			this.introStep(introLevel);
			return;
		}

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
