package de.siphalor.amecs.impl.mixin.controlling;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.blamejared.controlling.client.NewKeyBindsScreen;
import com.blamejared.controlling.platform.IPlatformHelper;

import de.siphalor.amecs.impl.duck.IKeybindsScreen;
import de.siphalor.amecs.impl.mixinimpl.MixinKeybindsScreenImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Key;

@Pseudo
@Mixin(NewKeyBindsScreen.class)
public abstract class MixinNewKeyBindsScreen extends KeybindsScreen implements IKeybindsScreen {

	// ignored
	public MixinNewKeyBindsScreen(Screen parent, GameOptions gameOptions) {
		super(parent, gameOptions);
	}

	@Inject(
		method = "mouseClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void onClicked(double x, double y, int type, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		MixinKeybindsScreenImpl.onClicked(selectedKeyBinding);
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
			target = "Lcom/blamejared/controlling/platform/IPlatformHelper;setKey(Lnet/minecraft/client/option/GameOptions;Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void clearKeyBinding(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		MixinKeybindsScreenImpl.clearKeyBinding(selectedKeyBinding);
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
			target = "Lcom/blamejared/controlling/platform/IPlatformHelper;setKey(Lnet/minecraft/client/option/GameOptions;Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void onKeyPressed(int keyCode, int scanCode, int int_3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		MixinKeybindsScreenImpl.onKeyPressed(this, keyCode, scanCode, selectedKeyBinding);
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
			target = "Lcom/blamejared/controlling/platform/IPlatformHelper;setKey(Lnet/minecraft/client/option/GameOptions;Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/util/InputUtil$Key;)V",
			ordinal = 0))
	public void doNotSetKeyCode(IPlatformHelper platformHelper, GameOptions options, KeyBinding newKeyBinding, Key key) {
		// do nothing
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
			value = "FIELD",
			opcode = Opcodes.PUTFIELD,
			// Lcom/blamejared/controlling/client/NewKeyBindsScreen;
			target = "Lcom/blamejared/controlling/client/NewKeyBindsScreen;selectedKeyBinding:Lnet/minecraft/client/option/KeyBinding;",
			ordinal = 0))
	public void doNotResetSelectedKeyBinding(NewKeyBindsScreen screen, KeyBinding newKeyBinding) {
		// do nothing
	}
}
