package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Plays when a twilight portal opens: turquoise and golden sparkles rise from the water surface of the new portal for three seconds.
 */
public final class PortalActivationEffect {
	private static final int DURATION_TICKS = 60;
	private static final double SURFACE_HEIGHT = 0.89D;
	private static final DustParticleOptions TURQUOISE = new DustParticleOptions(new Vector3f(0.1F, 0.85F, 0.8F), 1.1F);
	private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.78F, 0.2F), 1.1F);
	private static final List<Effect> EFFECTS = new ArrayList<>();

	private record Effect(ServerLevel level, List<BlockPos> positions, int[] ticksLeft) {
	}

	public static void register() {
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> tick());
		NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> EFFECTS.clear());
	}

	public static void start(Level level, Collection<BlockPos> positions) {
		if (!(level instanceof ServerLevel serverLevel) || positions.isEmpty()) return;
		BlockPos center = positions.iterator().next();
		serverLevel.playSound(null, center, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.5F, 0.8F);
		EFFECTS.add(new Effect(serverLevel, List.copyOf(positions), new int[]{DURATION_TICKS}));
	}

	private static void tick() {
		for (Iterator<Effect> iterator = EFFECTS.iterator(); iterator.hasNext(); ) {
			Effect effect = iterator.next();
			for (BlockPos pos : effect.positions()) {
				if (effect.level().random.nextInt(3) != 0) continue;
				double x = pos.getX() + effect.level().random.nextDouble();
				double y = pos.getY() + SURFACE_HEIGHT;
				double z = pos.getZ() + effect.level().random.nextDouble();
				effect.level().sendParticles(effect.level().random.nextBoolean() ? TURQUOISE : GOLD, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
			if (--effect.ticksLeft()[0] <= 0) iterator.remove();
		}
	}

	private PortalActivationEffect() {
	}
}
