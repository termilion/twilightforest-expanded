package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.AshenLordModel;
import twilightforest.entity.boss.AshenLord;

public class AshenLordRenderer extends MobRenderer<AshenLord, AshenLordModel<AshenLord>> {

	public static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("ashen_lord.png");

	public AshenLordRenderer(EntityRendererProvider.Context context) {
		super(context, new AshenLordModel<>(context.bakeLayer(TFModelLayers.ASHEN_LORD)), 1.0F);
	}

	@Override
	public void render(AshenLord entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		// his death is a slow ascent, not the vanilla red tumble: hide deathTime from the flip and the red overlay
		int deathTime = entity.deathTime;
		entity.deathTime = 0;
		super.render(entity, entityYaw, partialTicks, stack, buffer, light);
		entity.deathTime = deathTime;
	}

	@Override
	public ResourceLocation getTextureLocation(AshenLord entity) {
		return TEXTURE;
	}
}
