package de.siphalor.amecs.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.amecs.impl.duck.IKeybindsScreen;
import de.siphalor.amecs.impl.mixinimpl.MixinKeybindsScreenImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

@Mixin(ControlsOptionsScreen.class)
public abstract class MixinControlsOptionsScreen extends GameOptionsScreen implements IKeybindsScreen {
	// ignored
	public MixinControlsOptionsScreen(Screen screen, GameOptions gameOptions, Text text) {
		super(screen, gameOptions, text);
	}

	@Shadow
	public KeyBinding focusedBinding;

	@Shadow
	public long time;

	@Override
	public void setSelectedKeyBinding(KeyBinding selectedKeyBinding) {
		focusedBinding = selectedKeyBinding;
	}

	@Override
	public KeyBinding getSelectedKeyBinding() {
		return focusedBinding;
	}

	@Override
	public void setLastKeyCodeUpdateTime(long lastKeyCodeUpdateTime) {
		time = lastKeyCodeUpdateTime;
	}

	@Inject(
		method = "mouseClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V"))
	public void onClicked(double x, double y, int type, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		MixinKeybindsScreenImpl.onClicked(focusedBinding);
	}

	@Inject(
		method = "keyPressed",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void clearKeyBinding(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		MixinKeybindsScreenImpl.clearKeyBinding(focusedBinding);
	}

	@Inject(
		method = "keyPressed",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 1),
		cancellable = true)
	public void onKeyPressed(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(true);

		MixinKeybindsScreenImpl.onKeyPressed(keyCode, scanCode, focusedBinding, time, gameOptions);

		time = Util.getMeasuringTimeMs();
		KeyBinding.updateKeysByCode();
	}
}
