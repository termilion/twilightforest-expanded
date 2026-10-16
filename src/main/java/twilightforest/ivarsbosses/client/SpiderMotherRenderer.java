package twilightforest.ivarsbosses.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import twilightforest.TwilightForestMod;
import twilightforest.ivarsbosses.SpiderMotherEntity;

public class SpiderMotherRenderer extends GeoEntityRenderer<SpiderMotherEntity> {
	public SpiderMotherRenderer(EntityRendererProvider.Context context) {
		super(context, new Model());
		this.shadowRadius = 1.7F;
	}

	private static class Model extends GeoModel<SpiderMotherEntity> {
		private static final ResourceLocation MODEL = TwilightForestMod.prefix("geo/spider_mother.geo.json");
		private static final ResourceLocation ANIMATION = TwilightForestMod.prefix("animations/spider_mother.animation.json");
		private static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("spider_mother/spider_mother.png");

		@Override
		public ResourceLocation getModelResource(SpiderMotherEntity entity) {
			return MODEL;
		}

		@Override
		public ResourceLocation getTextureResource(SpiderMotherEntity entity) {
			return TEXTURE;
		}

		@Override
		public ResourceLocation getAnimationResource(SpiderMotherEntity entity) {
			return ANIMATION;
		}
	}
}
