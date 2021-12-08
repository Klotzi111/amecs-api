package de.siphalor.amecs.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.duck.IKeybindsScreen;
import de.siphalor.amecs.impl.duck.IMouse;
import de.siphalor.amecs.impl.version.KeybindsScreenVersionHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

// TODO: Fix the priority when Mixin 0.8 is a thing and try again (-> MaLiLib causes incompatibilities)
@Mixin(value = Mouse.class, priority = -2000)
public class MixinMouse implements IMouse {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private double eventDeltaWheel;

	@Unique
	private boolean mouseScrolled_eventUsed;

	@Override
	public boolean amecs$getMouseScrolledEventUsed() {
		return mouseScrolled_eventUsed;
	}

	@Inject(
		method = "onMouseButton",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
			ordinal = 0),
		cancellable = true)
	private void onMouseButtonPriority(long window, int type, int state, int int_3, CallbackInfo callbackInfo) {
		if (state == 1 && KeyBindingManager.onKeyPressedPriority(InputUtil.Type.MOUSE.createFromCode(type))) {
			callbackInfo.cancel();
		}
	}

	// If this method changes make sure to also change the corresponding code in KTIG
	private void onScrollReceived(double deltaY, boolean manualDeltaWheel, float g) {
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
				return;
			}

			eventDeltaWheel -= scrollCount;
			// -from minecraft
		} else {
			scrollCount = (int) g;
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

	@Inject(
		method = "onMouseScroll",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z",
			ordinal = 0),
		locals = LocalCapture.CAPTURE_FAILHARD)
	private void isSpectator_onMouseScroll(long window, double rawX, double rawY, CallbackInfo callbackInfo, double deltaY, float g) {
		// we are here in the else branch of "this.client.currentScreen != null" meaning currentScreen == null
		if (AmecsAPI.TRIGGER_KEYBINDING_ON_SCROLL) {
			onScrollReceived(KeyBindingUtils.getLastScrollAmount(), false, g);
		}
	}

	@Inject(
		method = "onMouseScroll",
		at = @At(
			value = "INVOKE_ASSIGN",
			ordinal = 0,
			shift = At.Shift.AFTER,
			target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z"),
		locals = LocalCapture.CAPTURE_FAILSOFT)
	private void mouseScrolled_onMouseScrolled(long window, double d, double e, CallbackInfo ci, double amount, double mouseX, double mouseY, boolean handled) {
		mouseScrolled_eventUsed = handled;
		if (handled) {
			return;
		}

		if (client.currentScreen.passEvents) {
			if (AmecsAPI.TRIGGER_KEYBINDING_ON_SCROLL) {
				onScrollReceived(KeyBindingUtils.getLastScrollAmount(), true, 0);
			}
		}
	}

	@Inject(
		method = "onMouseScroll",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;",
			ordinal = 0),
		locals = LocalCapture.CAPTURE_FAILHARD,
		cancellable = true)
	private void onMouseScroll(long window, double rawX, double rawY, CallbackInfo callbackInfo, double deltaY) {
		InputUtil.Key keyCode = KeyBindingUtils.getKeyFromScroll(deltaY);

		// check if we have scroll input for the options screen
		Screen currentScreen = client.currentScreen;
		if (currentScreen != null && KeybindsScreenVersionHelper.ACTUAL_KEYBINDS_SCREEN_CLASS.isAssignableFrom(currentScreen.getClass())) {
			IKeybindsScreen screen = (IKeybindsScreen) currentScreen;

			KeyBinding focusedBinding = screen.amecs$getSelectedKeyBinding();
			if (focusedBinding != null) {
				if (!focusedBinding.isUnbound()) {
					KeyModifiers keyModifiers = ((IKeyBinding) focusedBinding).amecs$getKeyModifiers();
					keyModifiers.set(KeyModifier.fromKey(((IKeyBinding) focusedBinding).amecs$getBoundKey()), true);
				}
				client.options.setKeyCode(focusedBinding, keyCode);
				KeyBinding.updateKeysByCode();
				screen.amecs$setSelectedKeyBinding(null);
				// if we do we cancel the method because we do not want the current screen to get the scroll event
				callbackInfo.cancel();
				return;
			}
		}

		KeyBindingUtils.setLastScrollAmount(deltaY);
		if (KeyBindingManager.onKeyPressedPriority(keyCode)) {
			callbackInfo.cancel();
		}
	}

}
