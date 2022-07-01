package de.siphalor.amecs.impl.mixinimpl;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Level;

import de.klotzi111.fabricmultiversionhelper.api.mapping.MappingHelper;
import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class MixinKeybindsScreenImpl {

	private static final Field ControlsOptionsScreen_gameOptions;

	static {
		Class<?> targetClass = MinecraftVersionHelper.isMCVersionAtLeast("1.15") ? GameOptionsScreen.class : ControlsOptionsScreen.class;
		String INTERMEDIARY_gameOptions = MinecraftVersionHelper.isMCVersionAtLeast("1.15") ? "field_21336" : "field_2724";
		ControlsOptionsScreen_gameOptions = MappingHelper.mapAndGetField(targetClass, INTERMEDIARY_gameOptions, GameOptions.class);
	}

	public static GameOptions getGameOptions(Screen screen) {
		try {
			return (GameOptions) ControlsOptionsScreen_gameOptions.get(screen);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			AmecsAPI.log(Level.ERROR, "Failed to get field \"gameOptions\"");
			AmecsAPI.logException(Level.ERROR, e);
			return null;
		}
	}

	public static void onClicked(KeyBinding selectedKeyBinding) {
		InputUtil.Key key = ((IKeyBinding) selectedKeyBinding).amecs$getBoundKey();
		KeyModifiers keyModifiers = ((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers();
		if (!key.equals(InputUtil.UNKNOWN_KEY)) {
			keyModifiers.set(KeyModifier.fromKey(key), true);
		}
	}

	public static void clearKeyBinding(KeyBinding selectedKeyBinding) {
		((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers().unset();
	}

	public static void onKeyPressed(Screen screen, int keyCode, int scanCode, KeyBinding selectedKeyBinding) {
		GameOptions gameOptions = getGameOptions(screen);

		if (selectedKeyBinding.isUnbound()) {
			gameOptions.setKeyCode(selectedKeyBinding, InputUtil.fromKeyCode(keyCode, scanCode));
		} else {
			InputUtil.Key mainKey = ((IKeyBinding) selectedKeyBinding).amecs$getBoundKey();
			KeyModifiers keyModifiers = ((IKeyBinding) selectedKeyBinding).amecs$getKeyModifiers();
			KeyModifier mainKeyModifier = KeyModifier.fromKey(mainKey);
			KeyModifier keyModifier = KeyModifier.fromKeyCode(keyCode);
			if (mainKeyModifier != KeyModifier.NONE && keyModifier == KeyModifier.NONE) {
				keyModifiers.set(mainKeyModifier, true);
				gameOptions.setKeyCode(selectedKeyBinding, InputUtil.fromKeyCode(keyCode, scanCode));
			} else {
				keyModifiers.set(keyModifier, true);
				keyModifiers.cleanup(selectedKeyBinding);
			}
		}
	}

}
