package twilightforest.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import twilightforest.client.model.entity.animations.PrincessAuroraAnimations;
import twilightforest.entity.boss.PrincessAurora;

/**
 * Queen Mab model, ported from Feywild (MIT, SaphieNyako) MabModel.
 */
public class PrincessAuroraModel extends HierarchicalModel<PrincessAurora> {

	private final ModelPart mab;
	private final ModelPart head;

	public PrincessAuroraModel(ModelPart root) {
		this.mab = root.getChild("mab");
		this.head = this.mab.getChild("head");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition mab = partdefinition.addOrReplaceChild("mab", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 0.0F));

		addHead(mab, PartPose.offset(0.0F, -16.0F, 0.0F));

		mab.addOrReplaceChild("body", CubeListBuilder.create().texOffs(88, 56).addBox(-4.5F, -14.6667F, -2.1667F, 9.0F, 9.0F, 7.0F, new CubeDeformation(0.0F))
			.texOffs(86, 0).mirror().addBox(0.5F, -13.6667F, -3.1667F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
			.texOffs(86, 0).addBox(-4.5F, -13.6667F, -3.1667F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
			.texOffs(116, 90).addBox(-3.5F, -5.6667F, -1.1667F, 7.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.3333F, -1.3333F));

		PartDefinition right_arm = mab.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(113, 23).addBox(-0.5F, -2.0F, -2.0F, 4.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -12.0F, 0.0F, 0.0F, 0.0F, -0.48F));

		PartDefinition sword = right_arm.addOrReplaceChild("sword", CubeListBuilder.create().texOffs(237, 0).addBox(-1.0F, -2.4369F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
			.texOffs(246, 0).addBox(-1.5F, 1.5631F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(240, 5).addBox(-3.0F, -3.4369F, -1.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(229, 4).addBox(-1.5F, -5.4369F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
			.texOffs(228, 10).addBox(-2.0F, -32.4369F, -0.5F, 4.0F, 29.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 13.9369F, -0.5F, 1.5708F, -0.9163F, 1.5708F));

		sword.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(238, 13).addBox(0.0F, 0.0F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -34.4369F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition left_arm = mab.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(113, 23).mirror().addBox(-3.5F, -1.0F, -2.0F, 4.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -13.0F, 0.0F, 0.0F, 0.0F, 0.48F));

		left_arm.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.5753F, 7.4975F, 0.0F, 0.0F, 3.1416F, -0.3491F));

		PartDefinition leftwing = mab.addOrReplaceChild("leftwing", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.25F, -11.0F, 8.5F, 0.2758F, -1.1541F, -0.923F));

		leftwing.addOrReplaceChild("bottom_left_wing", CubeListBuilder.create().texOffs(0, 67).addBox(0.0F, 0.0F, 0.0F, 0.0F, 34.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		leftwing.addOrReplaceChild("upper_left_wing", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -24.0F, 0.0F, 0.0F, 24.0F, 44.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.9548F, -0.1675F, 0.0406F));

		PartDefinition rightwing = mab.addOrReplaceChild("rightwing", CubeListBuilder.create(), PartPose.offsetAndRotation(2.25F, -11.0F, 8.5F, 0.2758F, 1.1541F, 0.923F));

		rightwing.addOrReplaceChild("bottom_right_wing", CubeListBuilder.create().texOffs(0, 67).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 34.0F, 25.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		rightwing.addOrReplaceChild("upper_right_wing", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -24.0F, 0.0F, 0.0F, 24.0F, 44.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -0.9548F, 0.1675F, -0.0406F));

		PartDefinition skirt = mab.addOrReplaceChild("skirt", CubeListBuilder.create(), PartPose.ZERO);

		skirt.addOrReplaceChild("rightskirt", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -1.0F, -3.5F, 7.0F, 30.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1309F));

		skirt.addOrReplaceChild("leftskirt", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -1.0F, -3.5F, 7.0F, 30.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1309F));

		mab.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(112, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 19.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.5F, 0.0F));

		mab.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(112, 0).addBox(-2.0F, -0.5F, -2.0F, 4.0F, 19.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.5F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	/**
	 * Head with crown, ears and hair. Shared with {@link PrincessAuroraTrophyModel}.
	 */
	public static void addHead(PartDefinition parent, PartPose pose) {
		PartDefinition head = parent.addOrReplaceChild("head", CubeListBuilder.create().texOffs(56, 20).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
			.texOffs(21, 0).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), pose);

		PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(129, 3).addBox(-4.5F, -20.0F, -6.0F, 9.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 0.0F));

		crown.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 30).addBox(0.0F, -3.5F, 0.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -12.5F, -6.0F, 0.0F, 0.4363F, 0.0F));

		crown.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, 30).addBox(0.0F, -3.5F, 0.0F, 0.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -12.5F, -6.0F, 0.0F, -0.4363F, 0.0F));

		head.addOrReplaceChild("righthear", CubeListBuilder.create().texOffs(56, 0).addBox(0.0F, -3.25F, 0.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -3.75F, -2.0F, 0.0F, 0.48F, 0.0F));

		head.addOrReplaceChild("lefthear", CubeListBuilder.create().texOffs(56, 0).addBox(0.0F, -3.25F, 0.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -3.75F, -2.0F, 0.0F, -0.48F, 0.0F));

		PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(56, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.5F)), PartPose.ZERO);

		hair.addOrReplaceChild("leftfronthair", CubeListBuilder.create().texOffs(104, 23).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.1667F, -5.2333F, -0.0873F, 0.0F, 0.6981F));

		hair.addOrReplaceChild("rightfronthair", CubeListBuilder.create().texOffs(104, 23).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -10.1667F, -5.2333F, -0.0873F, 0.0F, -0.6981F));

		hair.addOrReplaceChild("backhair", CubeListBuilder.create().texOffs(88, 72).addBox(-6.0F, -1.25F, -0.5F, 12.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
			.texOffs(0, 37).addBox(-6.0F, 12.75F, 0.0F, 12.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.75F, 5.5F, 0.1309F, 0.0F, 0.0F));
	}

	@Override
	public void setupAnim(PrincessAurora entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.head.yRot = Mth.clamp(netHeadYaw, -30.0F, 30.0F) * Mth.DEG_TO_RAD;
		this.head.xRot = Mth.clamp(headPitch, -25.0F, 45.0F) * Mth.DEG_TO_RAD;

		this.animate(entity.flyingIdleAnimationState, PrincessAuroraAnimations.FLYING_IDLE, ageInTicks);
		this.animate(entity.flyingAnimationState, PrincessAuroraAnimations.FLYING, ageInTicks);
		this.animate(entity.channelAnimationState, PrincessAuroraAnimations.CHANNEL, ageInTicks);
		this.animate(entity.intimidationAnimationState, PrincessAuroraAnimations.INTIMIDATION, ageInTicks);
		this.animate(entity.attackAnimationState, PrincessAuroraAnimations.ATTACK, ageInTicks);
	}

	@Override
	public ModelPart root() {
		return this.mab;
	}
}
