package de.siphalor.amecs.impl.mixinimpl;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class MixinKeybindsScreenImpl {

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

	public static void onKeyPressed(int keyCode, int scanCode, KeyBinding selectedKeyBinding, GameOptions gameOptions) {
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
