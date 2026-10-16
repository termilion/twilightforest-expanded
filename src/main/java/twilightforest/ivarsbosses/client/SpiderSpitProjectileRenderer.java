package twilightforest.ivarsbosses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.ivarsbosses.SpiderSpitProjectileEntity;

public class SpiderSpitProjectileRenderer extends EntityRenderer<SpiderSpitProjectileEntity> {
   private static final int FULL_BRIGHT = 15728880;
   private final BlockRenderDispatcher blockRenderer;

   public SpiderSpitProjectileRenderer(Context context) {
      super(context);
      this.blockRenderer = context.getBlockRenderDispatcher();
   }

   public void render(SpiderSpitProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
      float pitch = Mth.rotLerp(partialTick, entity.xRotO, entity.getXRot());
      float spin = entity.getSpin(partialTick);
      poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
      poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
      poseStack.mulPose(Axis.XP.rotationDegrees(spin * 0.55F));
      poseStack.mulPose(Axis.YP.rotationDegrees(spin));
      poseStack.mulPose(Axis.ZP.rotationDegrees(spin * 0.35F));
      this.renderPart(Blocks.WHITE_STAINED_GLASS.defaultBlockState(), poseStack, buffer, 0.48F, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F);
      this.renderPart(Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), poseStack, buffer, 0.62F, 0.02, 0.0, 0.0, 0.0F, spin * 0.6F, 90.0F);
      this.renderPart(Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState(), poseStack, buffer, 0.52F, -0.08, 0.05, 0.0, 75.0F, 0.0F, spin * 0.45F);
      this.renderPart(Blocks.COBWEB.defaultBlockState(), poseStack, buffer, 0.42F, 0.08, -0.04, 0.02, spin * 0.25F, 45.0F, 20.0F);
      this.renderPart(Blocks.WHITE_STAINED_GLASS.defaultBlockState(), poseStack, buffer, 0.24F, 0.24, 0.08, 0.02, spin, 35.0F, 0.0F);
      this.renderPart(Blocks.WHITE_STAINED_GLASS.defaultBlockState(), poseStack, buffer, 0.2F, -0.22, -0.05, -0.06, 0.0F, spin * 0.9F, 55.0F);
      poseStack.popPose();
      super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
   }

   private void renderPart(
      BlockState state, PoseStack poseStack, MultiBufferSource buffer, float scale, double x, double y, double z, float rotX, float rotY, float rotZ
   ) {
      poseStack.pushPose();
      poseStack.translate(x, y, z);
      poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
      poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
      poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
      poseStack.translate(-0.5 * scale, -0.5 * scale, -0.5 * scale);
      poseStack.scale(scale, scale, scale);
      this.blockRenderer.renderSingleBlock(state, poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY);
      poseStack.popPose();
   }

   public ResourceLocation getTextureLocation(SpiderSpitProjectileEntity entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
