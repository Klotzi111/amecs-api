package de.siphalor.amecs.api.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

/**
 * This interface is used for input event handling and is (un-)registered in {@link InputHandlerManager}
 *
 * @see #handleInput
 * @see InputHandlerManager
 */
@Environment(EnvType.CLIENT)
public interface InputEventHandler {

	/**
	 * This method is called from {@link InputHandlerManager#handleInputEvents(MinecraftClient)}.
	 * The method body typically checks the key binding's wasPressed() in a loop. Example:
	 * <br>
	 *
	 * <pre>
	 * while (keyBinding.wasPressed()) {
	 * 	// do your action
	 * }
	 * </pre>
	 *
	 * @see InputHandlerManager#handleInputEvents(MinecraftClient)
	 *
	 * @param client
	 */
	public void handleInput(MinecraftClient client);

}
