package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.renderer.specialty.DynamicGeoEntityRenderer;
import twilightforest.TwilightForestMod;
import twilightforest.entity.monster.MadDwarf;

// GeckoLib port of The Dwarven Forge dwarf (MIT). Clothes, beard and hair are separate bones that get their own texture.
public class MadDwarfRenderer extends DynamicGeoEntityRenderer<MadDwarf> {
	private static final ResourceLocation BASE = TwilightForestMod.getModelTexture("mad_dwarf/base.png");
	private static final ResourceLocation EYES = TwilightForestMod.getModelTexture("mad_dwarf/eyes.png");
	private static final ResourceLocation MASON = TwilightForestMod.getModelTexture("mad_dwarf/mason.png");
	private static final ResourceLocation WARRIOR = TwilightForestMod.getModelTexture("mad_dwarf/warrior.png");
	private static final ResourceLocation[] BEARDS = {
		TwilightForestMod.getModelTexture("mad_dwarf/beard/black.png"),
		TwilightForestMod.getModelTexture("mad_dwarf/beard/red.png"),
		TwilightForestMod.getModelTexture("mad_dwarf/beard/blonde.png"),
		TwilightForestMod.getModelTexture("mad_dwarf/beard/brown.png")
	};

	// While the eye layer re-renders the model, every bone draws the eye texture with the emissive render type
	private boolean eyePass;

	public MadDwarfRenderer(EntityRendererProvider.Context context) {
		super(context, new Model());
		this.shadowRadius = 0.4F;
		// the dwarf model is 1.625 blocks tall, the hitbox stays redcap sized
		this.withScale(0.85F);

		this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
			@Nullable
			@Override
			protected ItemStack getStackForBone(GeoBone bone, MadDwarf entity) {
				boolean left = entity.getMainArm() == HumanoidArm.LEFT;
				return switch (bone.getName()) {
					case "left_hand" -> left ? entity.getMainHandItem() : entity.getOffhandItem();
					case "right_hand" -> left ? entity.getOffhandItem() : entity.getMainHandItem();
					default -> null;
				};
			}

			@Override
			protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, MadDwarf entity) {
				return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
			}

			@Override
			protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, MadDwarf entity, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
				poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
				super.renderStackForBone(poseStack, bone, stack, entity, bufferSource, partialTick, packedLight, packedOverlay);
			}
		});

		this.addRenderLayer(new GeoRenderLayer<>(this) {
			@Override
			public void render(PoseStack poseStack, MadDwarf entity, BakedGeoModel model, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
				if (entity.isInvisible()) return;
				RenderType eyes = RenderType.eyes(EYES);
				MadDwarfRenderer.this.eyePass = true;
				try {
					MadDwarfRenderer.this.reRender(model, poseStack, bufferSource, entity, eyes, bufferSource.getBuffer(eyes), partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
				} finally {
					MadDwarfRenderer.this.eyePass = false;
				}
			}
		});
	}

	@Nullable
	@Override
	protected ResourceLocation getTextureOverrideForBone(GeoBone bone, MadDwarf entity, float partialTick) {
		if (this.eyePass) return EYES;

		return switch (bone.getName()) {
			case "clothes", "helmet", "left_sleeve", "right_sleeve", "left_leg_swing", "right_leg_swing" -> entity.isWarrior() ? WARRIOR : MASON;
			case "beard", "hair" -> BEARDS[entity.getBeardVariant()];
			default -> null;
		};
	}

	@Nullable
	@Override
	protected RenderType getRenderTypeOverrideForBone(GeoBone bone, MadDwarf entity, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return this.eyePass ? RenderType.eyes(texture) : super.getRenderTypeOverrideForBone(bone, entity, texture, bufferSource, partialTick);
	}

	private static class Model extends GeoModel<MadDwarf> {
		private static final ResourceLocation MODEL = TwilightForestMod.prefix("geo/mad_dwarf.geo.json");
		private static final ResourceLocation ANIMATION = TwilightForestMod.prefix("animations/mad_dwarf.animation.json");

		@Override
		public ResourceLocation getModelResource(MadDwarf entity) {
			return MODEL;
		}

		@Override
		public ResourceLocation getTextureResource(MadDwarf entity) {
			return BASE;
		}

		@Override
		public ResourceLocation getAnimationResource(MadDwarf entity) {
			return ANIMATION;
		}

		@Override
		public void setCustomAnimations(MadDwarf entity, long instanceId, AnimationState<MadDwarf> state) {
			AnimationProcessor<MadDwarf> bones = this.getAnimationProcessor();
			EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);

			var head = bones.getBone("head");
			if (head != null) {
				head.setRotX(data.headPitch() * Mth.DEG_TO_RAD);
				head.setRotY(data.netHeadYaw() * Mth.DEG_TO_RAD);
			}

			var leftLeg = bones.getBone("left_leg_swing");
			var rightLeg = bones.getBone("right_leg_swing");
			var leftArm = bones.getBone("left_arm_swing");
			var rightArm = bones.getBone("right_arm_swing");
			if (leftLeg == null || rightLeg == null || leftArm == null || rightArm == null) return;

			float swing = Mth.cos(state.getLimbSwing() * 0.6662F) * 1.4F * state.getLimbSwingAmount();
			leftLeg.setRotX(-swing);
			rightLeg.setRotX(swing);

			if (entity.isAggressive()) {
				// zombie-style raised arms that drop on every swing
				float strike = Mth.sin(entity.getAttackAnim(state.getPartialTick()) * Mth.PI);
				float raised = 1.5F - strike * 1.0F;
				float bob = Mth.sin(entity.tickCount * 0.09F) * 0.05F;
				leftArm.setRotX(raised + bob);
				rightArm.setRotX(raised - bob);
			} else {
				leftArm.setRotX(swing * 0.7F);
				rightArm.setRotX(-swing * 0.7F);
			}
			leftArm.setRotZ(0.0F);
			rightArm.setRotZ(0.0F);
		}
	}
}
