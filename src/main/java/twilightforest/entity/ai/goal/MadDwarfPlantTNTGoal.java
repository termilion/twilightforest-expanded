package twilightforest.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.EventHooks;
import twilightforest.entity.monster.MadDwarf;

public class MadDwarfPlantTNTGoal extends MadDwarfBaseGoal {

	public MadDwarfPlantTNTGoal(MadDwarf madDwarf) {
		super(madDwarf);
	}

	@Override
	public boolean canUse() {
		LivingEntity attackTarget = this.madDwarf.getTarget();
		return attackTarget != null
			&& !this.madDwarf.heldTNT.isEmpty()
			&& this.madDwarf.distanceToSqr(attackTarget) < 25
			&& !this.isTargetLookingAtMe(attackTarget)
			&& EventHooks.canEntityGrief(this.madDwarf.level(), this.madDwarf)
			&& !this.isLitTNTNearby(8)
			&& this.findBlockTNTNearby(5) == null;
	}

	@Override
	public void start() {
		BlockPos entityPos = new BlockPos(this.madDwarf.blockPosition());

		this.madDwarf.setItemSlot(EquipmentSlot.MAINHAND, this.madDwarf.heldTNT);

		if (this.madDwarf.level().isEmptyBlock(entityPos)) {
			this.madDwarf.heldTNT.shrink(1);
			this.madDwarf.playAmbientSound();
			this.madDwarf.level().setBlockAndUpdate(entityPos, Blocks.TNT.defaultBlockState());
			this.madDwarf.gameEvent(GameEvent.BLOCK_PLACE);
		}
	}

	@Override
	public void stop() {
		this.madDwarf.setItemSlot(EquipmentSlot.MAINHAND, this.madDwarf.heldPick);
	}
}
