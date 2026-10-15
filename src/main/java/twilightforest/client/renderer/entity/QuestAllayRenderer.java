package twilightforest.client.renderer.entity;

import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.allay.Allay;
import twilightforest.TwilightForestMod;

// Vanilla allay model with a purple (hue shifted) texture
public class QuestAllayRenderer extends AllayRenderer {
	private static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("quest_allay.png");

	public QuestAllayRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(Allay entity) {
		return TEXTURE;
	}
}
