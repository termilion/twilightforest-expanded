package twilightforest.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.Shroomling;

// GeckoLib model + animations ported from the Mobs of Mythology sporeling, one texture per variant
public class ShroomlingRenderer extends GeoEntityRenderer<Shroomling> {
	private static final ResourceLocation RED = TwilightForestMod.getModelTexture("shroomling/shroomling_red.png");
	private static final ResourceLocation BROWN = TwilightForestMod.getModelTexture("shroomling/shroomling_brown.png");

	public ShroomlingRenderer(EntityRendererProvider.Context context) {
		super(context, new Model());
		this.shadowRadius = 0.32F;
	}

	private static class Model extends DefaultedEntityGeoModel<Shroomling> {
		private static final ResourceLocation MODEL = TwilightForestMod.prefix("geo/shroomling.geo.json");
		private static final ResourceLocation ANIMATION = TwilightForestMod.prefix("animations/shroomling.animation.json");

		Model() {
			super(TwilightForestMod.prefix("shroomling"));
		}

		@Override
		public ResourceLocation getModelResource(Shroomling entity) {
			return MODEL;
		}

		@Override
		public ResourceLocation getAnimationResource(Shroomling entity) {
			return ANIMATION;
		}

		@Override
		public ResourceLocation getTextureResource(Shroomling entity) {
			return entity.getVariant() == Shroomling.Variant.RED ? RED : BROWN;
		}
	}
}
