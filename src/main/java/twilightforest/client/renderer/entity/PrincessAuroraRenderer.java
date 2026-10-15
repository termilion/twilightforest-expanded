package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.entity.boss.PrincessAurora;

public class PrincessAuroraRenderer<T extends PrincessAurora, M extends HumanoidModel<T>> extends HumanoidMobRenderer<T, M> {

	public static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("princessaurora.png");

	public PrincessAuroraRenderer(EntityRendererProvider.Context context, M model) {
		super(context, model, 0.625F);
	}

	@Override
	public ResourceLocation getTextureLocation(PrincessAurora entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(PrincessAurora entity, PoseStack stack, float partialTicks) {
		float scale = 1.2F;
		stack.scale(scale, scale, scale);
	}
}
