package twilightforest.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import twilightforest.entity.monster.MadDwarf;

import java.util.EnumSet;

public class MadDwarfLightTNTGoal extends MadDwarfBaseGoal {

	private final float pursueSpeed;
	private int delay;
	private BlockPos tntPos = null;

	@SuppressWarnings("this-escape")
	public MadDwarfLightTNTGoal(MadDwarf hostEntity, float speed) {
		super(hostEntity);
		this.pursueSpeed = speed;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (!EventHooks.canEntityGrief(this.madDwarf.level(), this.madDwarf)) {
			return false;
		}

		if (this.delay > 0) {
			--this.delay;
			return false;
		}

		BlockPos nearbyTNT = this.findBlockTNTNearby(8);
		if (nearbyTNT != null) {
			this.tntPos = nearbyTNT;
			return true;
		}

		return false;
	}

	@Override
	public boolean canContinueToUse() {
		return this.madDwarf.level().getBlockState(this.tntPos).is(Blocks.TNT);
	}

	@Override
	public void start() {
		this.madDwarf.setItemSlot(EquipmentSlot.MAINHAND, this.madDwarf.heldFlint);
	}

	@Override
	public void stop() {
		this.madDwarf.getNavigation().stop();
		this.madDwarf.setItemSlot(EquipmentSlot.MAINHAND, this.madDwarf.heldPick);
		this.delay = 20;
		this.tntPos = null;
	}

	@Override
	public void tick() {
		this.madDwarf.getLookControl().setLookAt(this.tntPos.getX(), this.tntPos.getY(), this.tntPos.getZ(), 30.0F, this.madDwarf.getMaxHeadXRot());

		if (this.madDwarf.distanceToSqr(Vec3.atLowerCornerOf(this.tntPos)) < 2.4D * 2.4D) {
			madDwarf.playAmbientSound();

			Blocks.TNT.onCaughtFire(Blocks.TNT.defaultBlockState(), this.madDwarf.level(), this.tntPos, Direction.UP, this.madDwarf);
			this.madDwarf.swing(InteractionHand.MAIN_HAND);
			this.madDwarf.level().setBlock(this.tntPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			this.madDwarf.gameEvent(GameEvent.PRIME_FUSE);
			this.madDwarf.getNavigation().stop();
		} else {
			this.madDwarf.getNavigation().moveTo(this.tntPos.getX(), this.tntPos.getY(), this.tntPos.getZ(), this.pursueSpeed);
		}
	}
}
