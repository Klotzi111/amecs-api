package de.siphalor.amecs.impl.mixinimpl;

import de.siphalor.amecs.api.KeyBindingUtils;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class MixinMouseImpl {

	/**
	 * This method does the actual logic when a mouse scroll is received
	 * <br>
	 * If this method changes make sure to also change the corresponding code in KTIG
	 *
	 * @param eventDeltaWheel
	 * @param deltaY
	 * @param manualDeltaWheel
	 * @param g
	 * @return the modified eventDeltaWheel
	 */
	public static double onScrollReceived(double eventDeltaWheel, double deltaY, boolean manualDeltaWheel, int g) {
		int scrollCount;
		if (manualDeltaWheel) {
			// from minecraft but patched
			// this code might be wrong when the vanilla mc code changes
			if (eventDeltaWheel != 0.0D && Math.signum(deltaY) != Math.signum(eventDeltaWheel)) {
				eventDeltaWheel = 0.0D;
			}

			eventDeltaWheel += deltaY;
			scrollCount = (int) eventDeltaWheel;
			if (scrollCount == 0) {
				return eventDeltaWheel;
			}

			eventDeltaWheel -= scrollCount;
			// -from minecraft
		} else {
			scrollCount = g;
		}

		InputUtil.Key keyCode = KeyBindingUtils.getKeyFromScroll(scrollCount);

		KeyBinding.setKeyPressed(keyCode, true);
		scrollCount = Math.abs(scrollCount);

		while (scrollCount > 0) {
			KeyBinding.onKeyPressed(keyCode);
			scrollCount--;
		}
		KeyBinding.setKeyPressed(keyCode, false);

		return eventDeltaWheel;
		// default minecraft scroll logic is in HotbarScrollKeyBinding in amecs
	}

}
