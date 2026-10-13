package twilightforest.entity.passive;

import twilightforest.data.tags.BlockTagGenerator;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFSounds;

public class Raven extends FlyingBird {

	/**
	 * Like {@link net.minecraft.world.entity.animal.Animal#checkAnimalSpawnRules}, but also on the bare ground of the amethyst straits
	 */
	public static boolean checkRavenSpawnRules(EntityType<? extends Raven> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
		return level.getBlockState(pos.below()).is(BlockTagGenerator.RAVEN_SPAWNABLE_ON) && isBrightEnoughToSpawn(level, pos);
	}

	public Raven(EntityType<? extends Raven> type, Level world) {
		super(type, world);
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return FlyingBird.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 10.0D)
			.add(Attributes.MOVEMENT_SPEED, 0.2D)
			.add(Attributes.STEP_HEIGHT, 1.0D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TFSounds.RAVEN_CAW.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TFSounds.RAVEN_SQUAWK.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TFSounds.RAVEN_SQUAWK.get();
	}

	@Override
	public boolean isSpooked() {
		return this.getLastHurtByMob() != null;
	}

	@Override
	public TagKey<Item> getTemptItems() {
		return ItemTagGenerator.RAVEN_TEMPT_ITEMS;
	}
}
