package twilightforest.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Replaces the vanilla action bar rendering for messages wider than {@link #MAX_WIDTH}: they are wrapped onto several lines that
 * grow upwards from the vanilla position, and stay on screen longer (reading time by length) the first frame they are drawn.
 * Short messages are left to vanilla. Used for the brownie's dialogue lines.
 */
public final class WrappedOverlayMessage {
	private static final int MAX_WIDTH = 320;
	private static final int TICKS_PER_CHAR = 1;
	private static final int MAX_TICKS = 300;

	@Nullable
	private static Component lastMessage;

	private WrappedOverlayMessage() {
	}

	public static void render(RenderGuiLayerEvent.Pre event) {
		if (event.getName() != VanillaGuiLayers.OVERLAY_MESSAGE)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		Gui gui = minecraft.gui;
		Component message = gui.overlayMessageString;
		if (message == null || gui.overlayMessageTime <= 0)
			return;

		GuiGraphics graphics = event.getGuiGraphics();
		Font font = gui.getFont();
		int maxWidth = Math.min(MAX_WIDTH, graphics.guiWidth() - 20);
		if (font.width(message) <= maxWidth)
			return;
		event.setCanceled(true);

		// A new message (vanilla resets the time to 60 ticks): give it time to be read
		if (message != lastMessage) {
			lastMessage = message;
			gui.overlayMessageTime = Mth.clamp(message.getString().length() * TICKS_PER_CHAR, 60, MAX_TICKS);
		}

		// Same fade and position as Gui#renderOverlayMessage
		float time = gui.overlayMessageTime - event.getPartialTick().getGameTimeDeltaPartialTick(false);
		int alpha = Math.min((int) (time * 255.0F / 20.0F), 255);
		if (alpha <= 8)
			return;
		int color = gui.animateOverlayMessageColor ? Mth.hsvToArgb(time / 50.0F, 0.7F, 0.6F, alpha) : FastColor.ARGB32.color(alpha, -1);
		int yShift = Math.max(gui.leftHeight, gui.rightHeight) + (68 - 59);

		List<FormattedCharSequence> lines = font.split(message, maxWidth);
		int lineHeight = font.lineHeight + 1;
		int top = -4 - (lines.size() - 1) * lineHeight;
		int width = lines.stream().mapToInt(font::width).max().orElse(0);
		graphics.pose().pushPose();
		graphics.pose().translate((float) (graphics.guiWidth() / 2), (float) (graphics.guiHeight() - Math.max(yShift, 68)), 0.0F);
		// One backdrop behind all lines (like GuiGraphics#drawStringWithBackdrop, which would overlap per line)
		int background = minecraft.options.getBackgroundColor(0.0F);
		if (background != 0)
			graphics.fill(-width / 2 - 2, top - 2, -width / 2 + width + 2, -4 + font.lineHeight + 2, FastColor.ARGB32.multiply(background, color));
		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			graphics.drawString(font, line, -font.width(line) / 2, top + i * lineHeight, color, true);
		}
		graphics.pose().popPose();
	}
}
