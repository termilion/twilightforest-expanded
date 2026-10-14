package twilightforest.palemonarch.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import twilightforest.TwilightForestMod;
import twilightforest.palemonarch.potion.OverencumberedMobEffect;
import twilightforest.palemonarch.potion.TiredMobEffect;

public class PaleMonarchModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, TwilightForestMod.ID);

	public static final DeferredHolder<MobEffect, MobEffect> OVERENCUMBERED = REGISTRY.register("overencumbered", OverencumberedMobEffect::new);
	public static final DeferredHolder<MobEffect, MobEffect> TIRED = REGISTRY.register("tired", TiredMobEffect::new);
}
