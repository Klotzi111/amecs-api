package de.siphalor.amecs.impl.mixin.versioned;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.duck.IMouse;
import de.siphalor.amecs.impl.mixinimpl.MixinMouseImpl;
import net.minecraft.client.Mouse;

// TODO: Fix the priority when Mixin 0.8 is a thing and try again (-> MaLiLib causes incompatibilities)
@Mixin(value = Mouse.class, priority = -2000)
public abstract class MixinMouse_1_14 implements IMouse {

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
			MixinMouseImpl.onScrollReceived(this, KeyBindingUtils.getLastScrollAmount(), false, (int) g);
		}
	}

}
