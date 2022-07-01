package de.siphalor.amecs.impl.version;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Level;

import de.klotzi111.fabricmultiversionhelper.api.mapping.MappingHelper;
import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.siphalor.amecs.impl.AmecsAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class MinecraftClientVersionHelper {

	private static final Field MinecraftClient_window;

	static {
		if (!MinecraftVersionHelper.isMCVersionAtLeast("1.15")) {
			MinecraftClient_window = MappingHelper.mapAndGetField(MinecraftClient.class, "field_1704", Window.class);
		} else {
			MinecraftClient_window = null;
		}
	}

	public static Window getWindow(MinecraftClient client) {
		if (MinecraftClient_window == null) {
			return client.getWindow();
		} else {
			try {
				return (Window) MinecraftClient_window.get(client);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				AmecsAPI.log(Level.ERROR, "Failed to get field \"MinecraftClient::window\"");
				AmecsAPI.logException(Level.ERROR, e);
				return null;
			}
		}
	}

}
