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
import twilightforest.palemonarch.entity.FalseKing1Entity;
import twilightforest.palemonarch.entity.FalseKing2Entity;
import twilightforest.palemonarch.entity.PaleMonarchDeathEntityEntity;
import twilightforest.palemonarch.entity.PaleMonarchEntity;
import twilightforest.palemonarch.entity.ShadowEntity;
import twilightforest.palemonarch.init.PaleMonarchModEntities;
import twilightforest.palemonarch.init.PaleMonarchModItems;
import twilightforest.palemonarch.init.PaleMonarchModMobEffects;

import java.util.ArrayList;
import java.util.Collection;
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
		List<Tuple<Runnable, Integer>> actions = new ArrayList<>();
		workQueue.forEach(work -> {
			work.setB(work.getB() - 1);
			if (work.getB() == 0) {
				actions.add(work);
			}
		});
		actions.forEach(e -> e.getA().run());
		workQueue.removeAll(actions);
	}

	// The server writes the clip name into synced data; every side copies it into the field the GeckoLib controllers read.
	private static void syncAnimations(EntityTickEvent.Pre event) {
		switch (event.getEntity()) {
			case PaleMonarchEntity e -> {
				String animation = e.getSyncedAnimation();
				if (!animation.equals("undefined")) {
					e.setAnimation("undefined");
					e.animationprocedure = animation;
				}
			}
			case PaleMonarchDeathEntityEntity e -> {
				String animation = e.getSyncedAnimation();
				if (!animation.equals("undefined")) {
					e.setAnimation("undefined");
					e.animationprocedure = animation;
				}
			}
			case FalseKing1Entity e -> {
				String animation = e.getSyncedAnimation();
				if (!animation.equals("undefined")) {
					e.setAnimation("undefined");
					e.animationprocedure = animation;
				}
			}
			case FalseKing2Entity e -> {
				String animation = e.getSyncedAnimation();
				if (!animation.equals("undefined")) {
					e.setAnimation("undefined");
					e.animationprocedure = animation;
				}
			}
			case ShadowEntity e -> {
				String animation = e.getSyncedAnimation();
				if (!animation.equals("undefined")) {
					e.setAnimation("undefined");
					e.animationprocedure = animation;
				}
			}
			default -> {
			}
		}
	}

	private static void addToTabs(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.COMBAT) {
			event.accept(PaleMonarchModItems.PALE_AXE.get());
		} else if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(PaleMonarchModItems.PALE_MONARCH_SPAWN_EGG.get());
		}
	}
}
