package de.siphalor.amecs.impl.mixinimpl;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.impl.duck.IMouse;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class MixinMouseImpl {

	/**
	 * This method does the actual logic when a mouse scroll is received
	 * <br>
	 * If this method changes make sure to also change the corresponding code in KTIG
	 *
	 * @param _this
	 * @param deltaY
	 * @param manualDeltaWheel
	 * @param g
	 */
	public static void onScrollReceived(IMouse _this, double deltaY, boolean manualDeltaWheel, int g) {
		int scrollCount;
		if (manualDeltaWheel) {
			double eventDeltaWheel = _this.amecs$getEventDeltaWheel();
			// from minecraft but patched
			// this code might be wrong when the vanilla mc code changes
			if (eventDeltaWheel != 0.0D && Math.signum(deltaY) != Math.signum(eventDeltaWheel)) {
				eventDeltaWheel = 0.0D;
			}

			eventDeltaWheel += deltaY;
			scrollCount = (int) eventDeltaWheel;
			if (scrollCount == 0) {
				_this.amecs$setEventDeltaWheel(eventDeltaWheel);
				return;
			}

			eventDeltaWheel -= scrollCount;
			_this.amecs$setEventDeltaWheel(eventDeltaWheel);
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

		// default minecraft scroll logic is in HotbarScrollKeyBinding in amecs
	}

}
