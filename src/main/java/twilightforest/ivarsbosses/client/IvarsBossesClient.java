package twilightforest.ivarsbosses.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import twilightforest.ivarsbosses.IvarsBosses;

public class IvarsBossesClient {
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(IvarsBosses.FENRIR.get(), FenrirRenderer::new);
		event.registerEntityRenderer(IvarsBosses.SPIDER_MOTHER.get(), SpiderMotherRenderer::new);
		event.registerEntityRenderer(IvarsBosses.SPIDER_SPIT.get(), SpiderSpitProjectileRenderer::new);
	}
}
