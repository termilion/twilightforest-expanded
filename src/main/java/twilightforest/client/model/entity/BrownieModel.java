package twilightforest.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import twilightforest.client.model.entity.animations.BrownieAnimations;
import twilightforest.entity.passive.Brownie;

// Geometry of the Friends & Foes rascal (Faboslav, CC BY-NC-ND 4.0), bone names kept for the converted animations
public class BrownieModel extends HierarchicalModel<Brownie> {
	private final ModelPart root;

	public BrownieModel(ModelPart root) {
		this.root = root;
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 52).addBox(-4.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
			.texOffs(28, 36).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 9.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 9.0F, -5.0F));
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 0.0F, -4.0F, 12.0F, 15.0F, 8.0F, new CubeDeformation(-0.01F))
			.texOffs(0, 23).addBox(-6.0F, 12.0F, -4.0F, 12.0F, 5.0F, 8.0F, new CubeDeformation(-0.5F))
			.texOffs(44, 0).addBox(3.0F, 0.0F, -4.0F, 2.0F, 15.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 4.0F, 0.0F));
		root.addOrReplaceChild("bag", CubeListBuilder.create().texOffs(0, 36).addBox(-4.0F, -0.5F, -0.5F, 8.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 7.5F, 4.5F));
		root.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(50, 28).mirror().addBox(-3.0F, -2.0F, -2.0F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 11.0F, 0.0F));
		root.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(50, 28).addBox(0.0F, -2.0F, -2.0F, 3.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 11.0F, 0.0F));
		root.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(28, 54).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 18.0F, 0.0F));
		root.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(28, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 18.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(Brownie entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animateWalk(BrownieAnimations.WALK, limbSwing, limbSwingAmount, 1.5F, 2.5F);
		this.animate(entity.idleAnimationState, BrownieAnimations.IDLE, ageInTicks);
		this.animate(entity.nodAnimationState, BrownieAnimations.NOD, ageInTicks);
		this.animate(entity.giveRewardAnimationState, BrownieAnimations.GIVE_REWARD, ageInTicks);
	}
}
