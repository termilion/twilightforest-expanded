package twilightforest.block.entity.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.entity.boss.AshenLord;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFEntities;
import twilightforest.init.TFParticleType;

public class AshenLordSpawnerBlockEntity extends BossSpawnerBlockEntity<AshenLord> {

	public AshenLordSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.ASHEN_LORD_SPAWNER.get(), TFEntities.ASHEN_LORD.get(), pos, state);
	}

	@Override
	public ParticleOptions getSpawnerParticle() {
		return TFParticleType.AUTUMN_SPARKLE.get();
	}
}
