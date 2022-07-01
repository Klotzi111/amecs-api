package de.siphalor.amecs.impl.version;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Level;

import de.klotzi111.fabricmultiversionhelper.api.mapping.MappingHelper;
import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.impl.AmecsAPI;
import net.minecraft.client.option.KeyBinding;

public class KeyBindingVersionHelper {

	private static final Field KeyBinding_pressed;

	static {
		if (!MinecraftVersionHelper.isMCVersionAtLeast("1.15")) {
			KeyBinding_pressed = MappingHelper.mapAndGetField(KeyBinding.class, "field_1653", boolean.class);
		} else {
			KeyBinding_pressed = null;
		}
	}

	@SuppressWarnings("deprecation")
	public static void setPressed(KeyBinding keyBinding, boolean pressed) {
		setPressedRaw(keyBinding, pressed);

		// this is required because the overridden method now no longer gets called and we need to add the functionality here as well
		if (keyBinding instanceof AmecsKeyBinding) {
			AmecsKeyBinding amecsKeyBinding = (AmecsKeyBinding) keyBinding;
			if (pressed) {
				amecsKeyBinding.onPressed();
			} else {
				amecsKeyBinding.onReleased();
			}
		}
	}

	public static void setPressedRaw(KeyBinding keyBinding, boolean pressed) {
		if (KeyBinding_pressed == null) {
			keyBinding.setPressed(pressed);
		} else {
			try {
				KeyBinding_pressed.setBoolean(keyBinding, pressed);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				AmecsAPI.log(Level.ERROR, "Failed to set field \"KeyBinding::pressed\"");
				AmecsAPI.logException(Level.ERROR, e);
			}
		}
	}

}
