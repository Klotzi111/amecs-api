package de.siphalor.amecs.impl.mixin.versioned;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.amecs.impl.duck.IKeybindsScreen;
import de.siphalor.amecs.impl.mixinimpl.MixinKeybindsScreenImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.text.Text;

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

	@Shadow
	private ControlsListWidget keyBindingListWidget;

	@Override
	public void amecs$setSelectedKeyBinding(KeyBinding selectedKeyBinding) {
		focusedBinding = selectedKeyBinding;
	}

	@Override
	public KeyBinding amecs$getSelectedKeyBinding() {
		return focusedBinding;
	}

	@Override
	public void amecs$setLastKeyCodeUpdateTime(long lastKeyCodeUpdateTime) {
		time = lastKeyCodeUpdateTime;
	}

	@Override
	public ControlsListWidget amecs$getControlsList() {
		return keyBindingListWidget;
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
		slice = @Slice(
			from = @At(
				value = "FIELD",
				opcode = Opcodes.GETSTATIC,
				target = "Lnet/minecraft/client/util/InputUtil;UNKNOWN_KEY:Lnet/minecraft/client/util/InputUtil$Key;",
				ordinal = 0)),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void clearKeyBinding(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		MixinKeybindsScreenImpl.clearKeyBinding(focusedBinding);
	}

	@Inject(
		method = "keyPressed",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;",
				ordinal = 0),
			to = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J",
				ordinal = 0)),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void onKeyPressed(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		MixinKeybindsScreenImpl.onKeyPressed(keyCode, scanCode, focusedBinding, gameOptions);
	}

	@Redirect(
		method = "keyPressed",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;",
				ordinal = 0),
			to = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J",
				ordinal = 0)),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void doNotSetKeyCode(GameOptions options, KeyBinding newKeyBinding, Key key) {
		// do nothing
	}

	@Redirect(
		method = "keyPressed",
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
				ordinal = 1),
			to = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J",
				ordinal = 0)),
		at = @At(
			value = "FIELD",
			opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/client/gui/screen/option/ControlsOptionsScreen;focusedBinding:Lnet/minecraft/client/option/KeyBinding;",
			ordinal = 0))
	public void doNotResetSelectedKeyBinding(ControlsOptionsScreen screen, KeyBinding newKeyBinding) {
		// do nothing
	}
}
