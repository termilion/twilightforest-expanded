package twilightforest.palemonarch.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.function.Function;

public class PaleMonarchGeoRenderer<T extends LivingEntity & GeoEntity> extends GeoEntityRenderer<T> {
	private final float scale;

	public PaleMonarchGeoRenderer(EntityRendererProvider.Context context, Function<T, String> texture, float scale) {
		super(context, new PaleMonarchGeoModel<>(texture));
		this.shadowRadius = 0.5F;
		this.scale = scale;
	}

	@Override
	public RenderType getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(this.getTextureLocation(animatable));
	}

	@Override
	public void preRender(PoseStack poseStack, T entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
		this.scaleHeight = this.scale;
		this.scaleWidth = this.scale;
		super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
	}

	@Override
	protected float getDeathMaxRotation(T entity) {
		return 0.0F;
	}
}
