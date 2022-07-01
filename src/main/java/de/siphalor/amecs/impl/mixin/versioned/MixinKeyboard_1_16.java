package de.siphalor.amecs.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.impl.KeyBindingManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;

@Mixin(Keyboard.class)
public class MixinKeyboard_1_16 {

	@Shadow
	private boolean repeatEvents;

	@Inject(
		method = "onKey",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screen/Screen;hasControlDown()Z",
			ordinal = 1,
			shift = Shift.BEFORE),
		cancellable = true)
	private void onKeyPriority(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		if (int_3 == 1 || (int_3 == 2 && repeatEvents)) {
			if (KeyBindingManager.onKeyPressedPriority(InputUtil.fromKeyCode(int_1, int_2))) {
				callbackInfo.cancel();
			}
		}
	}

}
