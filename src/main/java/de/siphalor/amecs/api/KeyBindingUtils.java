package de.siphalor.amecs.api;

import java.util.Map;

import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.impl.mixin.KeyBindingAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Utility methods and constants for Amecs and vanilla key bindings
 */
@Environment(EnvType.CLIENT)
public class KeyBindingUtils {
	public static final int MOUSE_SCROLL_UP = 512;
	public static final int MOUSE_SCROLL_DOWN = 513;

	private static double lastScrollAmount = 0;

	/**
	 * Gets the last (y directional) scroll delta
	 *
	 * @return the value
	 */
	public static double getLastScrollAmount() {
		return lastScrollAmount;
	}

	/**
	 * Sets the last (y directional) scroll amount. <b>For internal use only.</b>
	 *
	 * @param lastScrollAmount the amount
	 */
	public static void setLastScrollAmount(double lastScrollAmount) {
		KeyBindingUtils.lastScrollAmount = lastScrollAmount;
	}

	/**
	 * Gets the key object for the scroll direction
	 *
	 * @param deltaY the vertical (y) scroll amount {@link #getLastScrollAmount}
	 * @return the key object
	 */
	public static InputUtil.Key getKeyFromScroll(double deltaY) {
		return InputUtil.Type.MOUSE.createFromCode(deltaY > 0 ? KeyBindingUtils.MOUSE_SCROLL_UP : KeyBindingUtils.MOUSE_SCROLL_DOWN);
	}

	/**
	 * Gets the "official" idToKeys map.
	 * Name of the field as of '1.19+build.2': KEYS_BY_ID
	 *
	 * @deprecated This is Minecraft internal data and should not be accessible via this api. And the mapping for the field name is outdated
	 * @return the map (use with care)
	 */
	@Deprecated
	public static Map<String, KeyBinding> getIdToKeyBindingMap() {
		return KeyBindingAccessor.getKeysById();
	}

	/**
	 * Unregisters a keybinding from input querying but is NOT removed from the controls GUI
	 * <br>
	 * if you unregister a keybinding which is already in the controls GUI you can call {@link #registerHiddenKeyBinding(KeyBinding)} with this keybinding to undo this
	 * <br>
	 * <br>
	 * This is possible even after the game initialized
	 *
	 * @param keyBinding
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	public static boolean unregisterKeyBinding(KeyBinding keyBinding) {
		return unregisterKeyBinding(keyBinding.getTranslationKey());
	}

	/**
	 * Unregisters a keybinding with the given id
	 * <br>
	 * for more details {@link #unregisterKeyBinding(KeyBinding)}
	 *
	 * @see #unregisterKeyBinding(KeyBinding)
	 * @param id the translation key
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	public static boolean unregisterKeyBinding(String id) {
		KeyBinding keyBinding = getIdToKeyBindingMap().remove(id);
		return KeyBindingManager.unregister(keyBinding);
	}

	/**
	 * Registers a keybinding for input querying but is NOT added to the controls GUI
	 * <br>
	 * you can register a keybinding which is already in the controls GUI but was removed from input querying via {@link #unregisterKeyBinding(KeyBinding)}
	 * <br>
	 * <br>
	 * This is possible even after the game initialized
	 *
	 * @param keyBinding
	 * @return whether the keyBinding was added. It is not added if it is already contained
	 */
	public static boolean registerHiddenKeyBinding(KeyBinding keyBinding) {
		return KeyBindingManager.register(keyBinding);
	}
}
