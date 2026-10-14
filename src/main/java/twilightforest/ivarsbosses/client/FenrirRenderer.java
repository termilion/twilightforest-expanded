package twilightforest.ivarsbosses.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import twilightforest.TwilightForestMod;
import twilightforest.ivarsbosses.FenrirEntity;

public class FenrirRenderer extends GeoEntityRenderer<FenrirEntity> {
	public FenrirRenderer(EntityRendererProvider.Context context) {
		super(context, new Model());
		this.shadowRadius = 1.35F;
	}

	private static class Model extends GeoModel<FenrirEntity> {
		private static final ResourceLocation MODEL = TwilightForestMod.prefix("geo/fenrir.geo.json");
		private static final ResourceLocation ANIMATION = TwilightForestMod.prefix("animations/fenrir.animation.json");
		private static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("fenrir/fenrir.png");

		@Override
		public ResourceLocation getModelResource(FenrirEntity entity) {
			return MODEL;
		}

		@Override
		public ResourceLocation getTextureResource(FenrirEntity entity) {
			return TEXTURE;
		}

		@Override
		public ResourceLocation getAnimationResource(FenrirEntity entity) {
			return ANIMATION;
		}
	}
}
