package twilightforest.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import twilightforest.entity.ai.goal.MadDwarfPlantTNTGoal;
import twilightforest.init.TFItems;

public class MadDwarfWarrior extends MadDwarf {

	public MadDwarfWarrior(EntityType<? extends MadDwarfWarrior> type, Level world) {
		super(type, world);
		this.heldPick = new ItemStack(TFItems.IRONWOOD_PICKAXE.get());
		this.heldTNT.setCount(3);
	}

	@Override
	public boolean isWarrior() {
		return true;
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(4, new MadDwarfPlantTNTGoal(this));
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return MadDwarf.registerAttributes()
			.add(Attributes.MAX_HEALTH, 30.0D)
			.add(Attributes.ARMOR, 2.0D);
	}
}
