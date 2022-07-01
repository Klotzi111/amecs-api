package de.siphalor.amecs.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.mixinimpl.MixinKeyBindingImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding_1_16 implements IKeyBinding {

	@Inject(method = "getBoundKeyLocalizedText", at = @At("TAIL"), cancellable = true)
	public void inject_getBoundKeyLocalizedText(CallbackInfoReturnable<Text> callbackInfoReturnable) {
		Text fullName = MixinKeyBindingImpl.getModifierCompresedBoundKeyLocalizedText(this);
		callbackInfoReturnable.setReturnValue(fullName);
	}

}
