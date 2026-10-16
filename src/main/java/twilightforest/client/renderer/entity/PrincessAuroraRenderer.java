package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.PrincessAuroraModel;
import twilightforest.entity.boss.PrincessAurora;

public class PrincessAuroraRenderer extends MobRenderer<PrincessAurora, PrincessAuroraModel> {

	public static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("princessaurora.png");
	// the Feywild model is about 3.4 blocks tall, keep her the size of the old princess
	private static final float SCALE = 0.7F;

	public PrincessAuroraRenderer(EntityRendererProvider.Context context) {
		super(context, new PrincessAuroraModel(context.bakeLayer(TFModelLayers.PRINCESS_AURORA)), 0.5F * SCALE);
	}

	@Override
	public void render(PrincessAurora entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		// her death is a slow ascent, not the vanilla red tumble: hide deathTime from the flip and the red overlay
		int deathTime = entity.deathTime;
		entity.deathTime = 0;
		super.render(entity, entityYaw, partialTicks, stack, buffer, light);
		entity.deathTime = deathTime;
	}

	@Override
	public ResourceLocation getTextureLocation(PrincessAurora entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(PrincessAurora entity, PoseStack stack, float partialTicks) {
		stack.scale(SCALE, SCALE, SCALE);
	}
}
