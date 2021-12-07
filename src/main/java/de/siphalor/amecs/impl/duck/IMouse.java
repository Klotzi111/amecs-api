package de.siphalor.amecs.impl.duck;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public interface IMouse {
	// this is used by KTIG
	boolean amecs$getMouseScrolledEventUsed();
}
