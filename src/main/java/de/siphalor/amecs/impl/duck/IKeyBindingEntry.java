package de.siphalor.amecs.impl.duck;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public interface IKeyBindingEntry {
	KeyBinding amecs$getKeyBinding();
}
