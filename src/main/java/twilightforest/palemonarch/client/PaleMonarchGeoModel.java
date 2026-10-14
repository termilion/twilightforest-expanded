package twilightforest.palemonarch.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import twilightforest.TwilightForestMod;

import java.util.function.Function;

// All Pale Monarch entities share one geo model and one animation file; only the texture differs.
public class PaleMonarchGeoModel<T extends Entity & GeoAnimatable> extends GeoModel<T> {
	private static final ResourceLocation MODEL = TwilightForestMod.prefix("geo/pale_monarch.geo.json");
	private static final ResourceLocation ANIMATIONS = TwilightForestMod.prefix("animations/pale_monarch.animation.json");

	private final Function<T, String> texture;

	public PaleMonarchGeoModel(Function<T, String> texture) {
		this.texture = texture;
	}

	@Override
	public ResourceLocation getModelResource(T animatable) {
		return MODEL;
	}

	@Override
	public ResourceLocation getTextureResource(T animatable) {
		return TwilightForestMod.getModelTexture("pale_monarch/" + this.texture.apply(animatable) + ".png");
	}

	@Override
	public ResourceLocation getAnimationResource(T animatable) {
		return ANIMATIONS;
	}
}
