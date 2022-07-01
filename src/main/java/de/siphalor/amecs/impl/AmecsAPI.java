package de.siphalor.amecs.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class AmecsAPI implements ClientModInitializer {

	public static final String MOD_ID = "amecsapi";
	public static final String MOD_NAME = "Amecs API";

	private static final String LOG_PREFIX = "[" + MOD_NAME + "] ";
	public static Logger LOGGER = LogManager.getLogger();

	public static final KeyModifiers CURRENT_MODIFIERS = new KeyModifiers();

	// this is used by KTIG
	public static boolean TRIGGER_KEYBINDING_ON_SCROLL = true;

	public static String makeKeyID(String keyName) {
		return "key." + MOD_ID + "." + keyName;
	}

	@Override
	public void onInitializeClient() {

	}

	public static void log(Level level, String message) {
		LOGGER.log(level, LOG_PREFIX + message);
	}

	public static void logException(Level level, Throwable e) {
		LOGGER.catching(level, e);
	}
}
