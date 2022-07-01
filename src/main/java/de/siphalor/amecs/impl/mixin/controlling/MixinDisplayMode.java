package de.siphalor.amecs.impl.mixin.controlling;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.blamejared.controlling.api.DisplayMode;
import com.blamejared.controlling.client.NewKeyBindsList.KeyEntry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

@Pseudo
@Mixin(DisplayMode.class)
public abstract class MixinDisplayMode {

	@Shadow
	@Final
	@Mutable
	private Predicate<KeyEntry> predicate;

	@SuppressWarnings("resource")
	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void onConstruct(String name, int ordinal, Predicate<KeyEntry> predicate, CallbackInfo ci) {
		if (name.equals("CONFLICTING")) {
			this.predicate = keyEntry -> {
				KeyBinding thisKeyBinding = keyEntry.getKeybinding();
				if (thisKeyBinding.isUnbound()) {
					return false;
				}
				for (KeyBinding keyBinding : MinecraftClient.getInstance().options.allKeys) {
					if (keyBinding != thisKeyBinding && keyBinding.equals(thisKeyBinding)) {
						return true;
					}
				}
				return false;
			};
		}
	}

}
