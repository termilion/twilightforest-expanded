package twilightforest.block.entity.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.entity.boss.PrincessAurora;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFEntities;
import twilightforest.init.TFParticleType;

public class PrincessAuroraSpawnerBlockEntity extends BossSpawnerBlockEntity<PrincessAurora> {

	public PrincessAuroraSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.PRINCESS_AURORA_SPAWNER.get(), TFEntities.PRINCESS_AURORA.get(), pos, state);
	}

	@Override
	public boolean anyPlayerInRange(Level level) {
		Player closestPlayer = level.getNearestPlayer(this.getBlockPos().getX() + 0.5D, this.getBlockPos().getY() + 0.5D, this.getBlockPos().getZ() + 0.5D, this.getRange(), false);
		return closestPlayer != null && closestPlayer.getY() > this.getBlockPos().getY() - 4;
	}

	@Override
	public ParticleOptions getSpawnerParticle() {
		return TFParticleType.SNOW.get();
	}
}
