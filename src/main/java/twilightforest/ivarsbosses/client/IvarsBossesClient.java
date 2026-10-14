package twilightforest.ivarsbosses.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import twilightforest.ivarsbosses.IvarsBosses;

public class IvarsBossesClient {
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(IvarsBosses.FENRIR.get(), FenrirRenderer::new);
	}
}
