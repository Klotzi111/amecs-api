package de.siphalor.amecs.impl.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {

	@Accessor("KEYS_BY_ID")
	static Map<String, KeyBinding> getKeysById() {
		return null;
	}

}
