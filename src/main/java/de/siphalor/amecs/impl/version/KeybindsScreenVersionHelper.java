package de.siphalor.amecs.impl.version;

import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;

public class KeybindsScreenVersionHelper {

	public static final Class<?> ACTUAL_KEYBINDS_SCREEN_CLASS;

	public static final Class<?> KeybindsScreen_class;

	static {
		if (MinecraftVersionHelper.isMCVersionAtLeast("1.18")) {
			KeybindsScreen_class = KeybindsScreen.class;
			ACTUAL_KEYBINDS_SCREEN_CLASS = KeybindsScreen_class;
		} else {
			KeybindsScreen_class = null;
			ACTUAL_KEYBINDS_SCREEN_CLASS = ControlsOptionsScreen.class;
		}
	}

}
