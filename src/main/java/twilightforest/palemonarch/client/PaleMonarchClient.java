package twilightforest.palemonarch.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import twilightforest.palemonarch.entity.FalseKing1Entity;
import twilightforest.palemonarch.entity.FalseKing2Entity;
import twilightforest.palemonarch.entity.PaleMonarchDeathEntityEntity;
import twilightforest.palemonarch.entity.PaleMonarchEntity;
import twilightforest.palemonarch.entity.ShadowEntity;
import twilightforest.palemonarch.init.PaleMonarchModEntities;

public class PaleMonarchClient {
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(PaleMonarchModEntities.PALE_MONARCH.get(), ctx -> new PaleMonarchGeoRenderer<>(ctx, PaleMonarchEntity::getTexture, 1.025F));
		event.registerEntityRenderer(PaleMonarchModEntities.PALE_MONARCH_DEATH_ENTITY.get(), ctx -> new PaleMonarchGeoRenderer<>(ctx, PaleMonarchDeathEntityEntity::getTexture, 1.025F));
		event.registerEntityRenderer(PaleMonarchModEntities.FALSE_KING_1.get(), ctx -> new PaleMonarchGeoRenderer<>(ctx, FalseKing1Entity::getTexture, 1.0F));
		event.registerEntityRenderer(PaleMonarchModEntities.FALSE_KING_2.get(), ctx -> new PaleMonarchGeoRenderer<>(ctx, FalseKing2Entity::getTexture, 1.0F));
		event.registerEntityRenderer(PaleMonarchModEntities.SHADOW.get(), ctx -> new PaleMonarchGeoRenderer<>(ctx, ShadowEntity::getTexture, 1.0F));
	}
}
