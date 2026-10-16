package twilightforest.palemonarch.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.common.NeoForge;
import twilightforest.init.TFSounds;
import twilightforest.palemonarch.entity.PaleMonarchEntity;

/**
 * Plays the Radiance disc track as boss music while a living Pale Monarch is within his notice range.
 */
public class PaleMonarchMusic {
	private static final double RANGE = 32.0D;
	private static final Music BOSS_MUSIC = new Music(TFSounds.MUSIC_DISC_RADIANCE, 0, 0, true);

	// Updated once per client tick
	private static boolean nearBoss;

	public static void register() {
		NeoForge.EVENT_BUS.addListener(EventPriority.LOW, PaleMonarchMusic::selectMusic);
		NeoForge.EVENT_BUS.addListener(PaleMonarchMusic::stopWhenGone);
	}

	private static boolean isNearBoss() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) return false;
		return !minecraft.level.getEntitiesOfClass(PaleMonarchEntity.class, minecraft.player.getBoundingBox().inflate(RANGE), PaleMonarchEntity::isAlive).isEmpty();
	}

	private static void selectMusic(SelectMusicEvent event) {
		if (nearBoss) {
			event.setMusic(BOSS_MUSIC);
		}
	}

	// Vanilla only swaps tracks when the new music may replace the current one, so stop the boss track ourselves.
	private static void stopWhenGone(ClientTickEvent.Post event) {
		boolean near = isNearBoss();
		if (nearBoss && !near && Minecraft.getInstance().getMusicManager().isPlayingMusic(BOSS_MUSIC)) {
			Minecraft.getInstance().getMusicManager().stopPlaying();
		}
		nearBoss = near;
	}
}
