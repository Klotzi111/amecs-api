package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.duck.IKeybindsScreen;
import de.siphalor.amecs.impl.version.KeybindsScreenVersionHelper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Util;

@Mixin(Keyboard.class)
public class MixinKeyboard {

	@SuppressWarnings("resource")
	@Inject(
		method = "onKey",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J",
			ordinal = 0))
	private void onKey(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (currentScreen != null && KeybindsScreenVersionHelper.ACTUAL_KEYBINDS_SCREEN_CLASS.isAssignableFrom(currentScreen.getClass())) {
			// if it is the keyBinding screen

			if (int_3 == 0) {
				// Key released
				IKeybindsScreen screen = (IKeybindsScreen) currentScreen;

				screen.amecs$setSelectedKeyBinding(null);
				screen.amecs$setLastKeyCodeUpdateTime(Util.getMeasuringTimeMs());
			}

			// do not call keyBinding keyModifier update here
			// it is done after the key is entry finish
		}

		int keyCode = InputUtil.fromKeyCode(int_1, int_2).getCode();
		boolean pressed = int_3 != 0;
		AmecsAPI.CURRENT_MODIFIERS.set(KeyModifier.fromKeyCode(keyCode), pressed);
	}

}
