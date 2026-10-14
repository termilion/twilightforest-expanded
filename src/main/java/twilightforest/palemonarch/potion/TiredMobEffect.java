package twilightforest.palemonarch.potion;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.EffectCure;

@SuppressWarnings({"cast", "unchecked", "rawtypes", "removal", "static", "lossy-conversions"}) // decompiled MCreator code
public class TiredMobEffect extends MobEffect {
   public TiredMobEffect() {
      super(MobEffectCategory.NEUTRAL, -13421773);
      this.addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("twilightforest", "effect.tired_0"), -5.0, Operation.ADD_VALUE);
   }

   public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
   }
}
