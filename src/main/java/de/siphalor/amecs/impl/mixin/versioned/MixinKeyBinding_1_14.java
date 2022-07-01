package de.siphalor.amecs.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.klotzi111.fabricmultiversionhelper.api.text.IMutableText;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.mixinimpl.MixinKeyBindingImpl;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding_1_14 implements IKeyBinding {

	@Inject(method = "method_16007()Ljava/lang/String;", remap = false, at = @At("TAIL"), cancellable = true)
	public void inject_getBoundKeyLocalizedText(CallbackInfoReturnable<String> callbackInfoReturnable) {
		Text fullName = MixinKeyBindingImpl.getModifierCompresedBoundKeyLocalizedText(this);
		callbackInfoReturnable.setReturnValue(((IMutableText) fullName).fmvh$asString());
	}

}
