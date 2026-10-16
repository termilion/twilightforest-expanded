package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The Spider Mother's drop: a diamond-strength sword whose hits have a 30 % chance to poison the target (Poison II, 10 seconds).
 */
public class StingerItem extends SwordItem {
	private static final float POISON_CHANCE = 0.3F;
	private static final int POISON_TICKS = 10 * 20;

	public StingerItem(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!target.level().isClientSide() && attacker.getRandom().nextFloat() < POISON_CHANCE)
			target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 1), attacker);
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltips, flags);
		tooltips.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}
}
