package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.duck.IKeybindsScreen;
import de.siphalor.amecs.impl.version.KeybindsScreenVersionHelper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;

@Mixin(Keyboard.class)
public class MixinKeyboard {

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

	@SuppressWarnings("resource")
	@Inject(
		method = "onKey",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J",
			ordinal = 0))
	private void onKey(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		// Key released
		if (int_3 == 0) {
			Screen currentScreen = MinecraftClient.getInstance().currentScreen;
			if (currentScreen != null && KeybindsScreenVersionHelper.ACTUAL_KEYBINDS_SCREEN_CLASS.isAssignableFrom(currentScreen.getClass())) {
				IKeybindsScreen screen = (IKeybindsScreen) currentScreen;

				screen.setSelectedKeyBinding(null);
				screen.setLastKeyCodeUpdateTime(Util.getMeasuringTimeMs());
			}
		}

		AmecsAPI.CURRENT_MODIFIERS.set(KeyModifier.fromKeyCode(InputUtil.fromKeyCode(int_1, int_2).getCode()), int_3 != 0);
	}
}
