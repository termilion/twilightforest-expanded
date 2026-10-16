package twilightforest.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFParticleType;

import java.util.List;
import java.util.function.Supplier;

/**
 * Replicates the old "depthsuspend" void fog particles (removed from vanilla) that drifted above the bedrock layer,
 * but spawns them above the ground blocks of the amethyst straits: black motes over obsidian, grey motes over blackstone.
 */
public class ObsidianMoteEvents {
	private static final int SAMPLES = 1000;
	private static final int RANGE = 16;
	private static final int MAX_HEIGHT_ABOVE = 2;

	/**
	 * @param chance spawn chance out of 8 per sampled air block above an emitter (old void fog: 1 in 8)
	 */
	private record Mote(TagKey<Block> emitters, Supplier<? extends ParticleOptions> particle, int chance) {
	}

	private static final List<Mote> MOTES = List.of(
		new Mote(BlockTagGenerator.EMITS_OBSIDIAN_MOTES, TFParticleType.OBSIDIAN_MOTE, 3),
		new Mote(BlockTagGenerator.EMITS_BLACKSTONE_MOTES, TFParticleType.BLACKSTONE_MOTE, 1)
	);

	protected static void tickObsidianMotes(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Player player = mc.player;
		if (mc.isPaused() || level == null || player == null || !level.getBiome(player.blockPosition()).is(TFBiomes.AMETHYST_STRAITS))
			return;

		RandomSource random = level.getRandom();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
		BlockPos origin = player.blockPosition();

		// Same sampling as the old doVoidFogParticles: random air blocks around the player
		for (int i = 0; i < SAMPLES; i++) {
			pos.set(origin.getX() + random.nextInt(RANGE) - random.nextInt(RANGE), origin.getY() + random.nextInt(RANGE) - random.nextInt(RANGE), origin.getZ() + random.nextInt(RANGE) - random.nextInt(RANGE));
			if (!level.getBlockState(pos).isAir())
				continue;

			Mote mote = findMote(level, pos, below);
			if (mote != null && random.nextInt(8) < mote.chance() && level.getBiome(pos).is(TFBiomes.AMETHYST_STRAITS))
				level.addParticle(mote.particle().get(), pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
		}
	}

	private static Mote findMote(ClientLevel level, BlockPos pos, BlockPos.MutableBlockPos below) {
		for (int dy = 1; dy <= MAX_HEIGHT_ABOVE; dy++) {
			BlockState state = level.getBlockState(below.setWithOffset(pos, 0, -dy, 0));
			for (Mote mote : MOTES)
				if (state.is(mote.emitters()))
					return mote;
			if (!state.isAir())
				return null;
		}
		return null;
	}
}
