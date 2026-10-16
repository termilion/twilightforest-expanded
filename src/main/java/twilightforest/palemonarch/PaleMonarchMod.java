package twilightforest.palemonarch;

import net.minecraft.util.Tuple;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import twilightforest.palemonarch.client.PaleMonarchClient;
import twilightforest.palemonarch.client.PaleMonarchMusic;
import twilightforest.palemonarch.entity.SyncedAnimationEntity;
import twilightforest.palemonarch.init.PaleMonarchModEntities;
import twilightforest.palemonarch.init.PaleMonarchModItems;
import twilightforest.palemonarch.init.PaleMonarchModMobEffects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Entry point of the Pale Monarch port (boss, false king clones, king's shadow, pale axe).
 * Keeps the original mod's delayed-work queue that all attack procedures are built on.
 */
public class PaleMonarchMod {
	private static final Collection<Tuple<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void register(IEventBus modBus) {
		PaleMonarchModEntities.REGISTRY.register(modBus);
		PaleMonarchModItems.REGISTRY.register(modBus);
		PaleMonarchModMobEffects.REGISTRY.register(modBus);
		modBus.addListener(PaleMonarchModEntities::registerAttributes);
		modBus.addListener(PaleMonarchMod::addToTabs);
		if (FMLEnvironment.dist.isClient()) {
			modBus.addListener(PaleMonarchClient::registerEntityRenderers);
			PaleMonarchMusic.register();
		}
		NeoForge.EVENT_BUS.addListener(PaleMonarchMod::tick);
		NeoForge.EVENT_BUS.addListener(PaleMonarchMod::syncAnimations);
	}

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
			workQueue.add(new Tuple<>(action, tick));
		}
	}

	private static void tick(ServerTickEvent.Post event) {
		if (workQueue.isEmpty())
			return;

		List<Runnable> due = new ArrayList<>();
		for (Iterator<Tuple<Runnable, Integer>> iterator = workQueue.iterator(); iterator.hasNext(); ) {
			Tuple<Runnable, Integer> work = iterator.next();
			work.setB(work.getB() - 1);
			if (work.getB() == 0) {
				due.add(work.getA());
				iterator.remove();
			}
		}
		due.forEach(Runnable::run);
	}

	private static void syncAnimations(EntityTickEvent.Pre event) {
		if (event.getEntity() instanceof SyncedAnimationEntity entity)
			entity.syncAnimation();
	}

	private static void addToTabs(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.COMBAT) {
			event.accept(PaleMonarchModItems.PALE_AXE.get());
		} else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(PaleMonarchModItems.PALE_MONARCH_SPAWN_EGG.get());
		}
	}
}
