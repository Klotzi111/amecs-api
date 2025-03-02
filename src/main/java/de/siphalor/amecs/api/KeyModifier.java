package de.siphalor.amecs.api;

import org.apache.commons.lang3.ArrayUtils;

import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.ModifierPrefixTextProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public enum KeyModifier {
	// the order of the enums makes a difference when generating the shown name in the gui
	// with this order the old text order is preserved. But now the id values do not increment nicely. But changing them would eliminate
	// backward compatibility with the old save format
	NONE("none", -1),
	ALT("alt", 0, 342, 346),
	SHIFT("shift", 2, 340, 344),
	CONTROL("control", 1, 341, 345);

	// using this array for the values because it is faster than calling values() every time
	public static final KeyModifier[] VALUES = KeyModifier.values();

	public final String name;
	public final int id;
	public final ModifierPrefixTextProvider textProvider;
	// these keyCodes are all from Type: InputUtil.Type.KEYSYM
	final int[] keyCodes;

	private KeyModifier(String name, int id, int... keyCodes) {
		this.name = name;
		this.id = id;
		textProvider = new ModifierPrefixTextProvider(this);
		this.keyCodes = keyCodes;
	}

	public static KeyModifier fromKeyCode(int keyCode) {
		for (KeyModifier keyModifier : VALUES) {
			if (keyModifier == NONE) {
				continue;
			}
			if (keyModifier.matches(keyCode)) {
				return keyModifier;
			}
		}
		return NONE;
	}

	public static KeyModifier fromKey(InputUtil.Key key) {
		if (key == null || key.getCategory() != InputUtil.Type.KEYSYM) {
			return NONE;
		}
		return fromKeyCode(key.getCode());
	}

	public boolean matches(int keyCode) {
		return ArrayUtils.contains(keyCodes, keyCode);
	}

	public String getTranslationKey() {
		return AmecsAPI.MOD_ID + ".modifier." + name;
	}

	public static int getModifierCount() {
		return VALUES.length - 1; // remove 1 for NONE
	}
}
