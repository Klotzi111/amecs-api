package de.siphalor.amecs.impl.mixin.controlling;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.blamejared.controlling.client.NewKeyBindsList.KeyEntry;

import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.duck.IKeyBindingEntry;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Pseudo
@Mixin(KeyEntry.class)
public abstract class MixinKeyEntry implements IKeyBindingEntry {
	@Shadow
	@Final
	private KeyBinding keybinding;

	// + interface methods
	@Override
	public KeyBinding amecs$getKeyBinding() {
		return keybinding;
	}
	// - interface methods

	// + normal mixin
	@Inject(
		method = "lambda$new$1(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V",
		at = @At("RETURN"))
	private void onResetButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) keybinding).amecs$getKeyModifiers().unset();
		if (keybinding instanceof AmecsKeyBinding) {
			((AmecsKeyBinding) keybinding).resetKeyBinding();
		}
	}

	@Inject(
		method = "lambda$new$0(Lnet/minecraft/client/option/KeyBinding;Lnet/minecraft/client/gui/widget/ButtonWidget;)V",
		at = @At("HEAD"))
	private void onEditButtonClicked(KeyBinding keyBinding, ButtonWidget buttonWidget, CallbackInfo callbackInfo) {
		((IKeyBinding) keybinding).amecs$getKeyModifiers().unset();
		keybinding.setBoundKey(InputUtil.UNKNOWN_KEY);
	}
	// - normal mixin

}
