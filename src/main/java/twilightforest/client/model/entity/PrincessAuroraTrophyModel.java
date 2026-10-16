package twilightforest.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import twilightforest.client.renderer.entity.PrincessAuroraRenderer;

public class PrincessAuroraTrophyModel implements TrophyBlockModel {

	// the queen's head is 10 pixels wide, scale it down to skull size
	private static final float SCALE = 0.8F;

	private final ModelPart head;

	public PrincessAuroraTrophyModel(ModelPart root) {
		this.head = root.getChild("head");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PrincessAuroraModel.addHead(meshdefinition.getRoot(), PartPose.ZERO);
		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupRotationsForTrophy(float x, float y, float z, float mouthAngle) {
		this.head.yRot = y * Mth.DEG_TO_RAD;
		this.head.xRot = z * Mth.DEG_TO_RAD;
	}

	@Override
	public void renderTrophy(PoseStack stack, MultiBufferSource buffer, int light, int overlay, int color, ItemDisplayContext context) {
		stack.pushPose();
		stack.scale(SCALE, SCALE, SCALE);
		VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(PrincessAuroraRenderer.TEXTURE));
		this.head.render(stack, consumer, light, overlay, color);
		stack.popPose();
	}
}
