package twilightforest.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.BrownieModel;
import twilightforest.entity.passive.Brownie;

public class BrownieRenderer extends MobRenderer<Brownie, BrownieModel> {
	private static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("brownie.png");

	public BrownieRenderer(EntityRendererProvider.Context context) {
		super(context, new BrownieModel(context.bakeLayer(TFModelLayers.BROWNIE)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(Brownie entity) {
		return TEXTURE;
	}
}
