package twilightforest.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import twilightforest.TwilightForestMod;

/**
 * Head of the Pale Monarch as a vanilla model, the boss itself is a GeckoLib model.
 * Cubes copied from the "head" and "headwear" bones of geo/pale_monarch.geo.json.
 */
public class PaleMonarchTrophyModel implements TrophyBlockModel {

	public static final ResourceLocation TEXTURE = TwilightForestMod.prefix("textures/entity/pale_monarch/kingtexturestreamlined.png");

	private final ModelPart head;

	public PaleMonarchTrophyModel(ModelPart root) {
		this.head = root.getChild("head");
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
			PartPose.ZERO);
		head.addOrReplaceChild("headwear", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.5F)),
			PartPose.ZERO);
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupRotationsForTrophy(float x, float y, float z, float mouthAngle) {
		this.head.yRot = y * Mth.DEG_TO_RAD;
		this.head.xRot = z * Mth.DEG_TO_RAD;
	}

	@Override
	public void renderTrophy(PoseStack stack, MultiBufferSource buffer, int light, int overlay, int color, ItemDisplayContext context) {
		this.head.render(stack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, overlay, color);
	}
}
