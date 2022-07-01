package de.siphalor.amecs.impl.duck;

import org.jetbrains.annotations.ApiStatus;

import de.siphalor.amecs.api.KeyModifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public interface IKeyBinding {
	InputUtil.Key amecs$getBoundKey();

	int amecs$getTimesPressed();

	void amecs$setTimesPressed(int timesPressed);

	void amecs$incrementTimesPressed();

	void amecs$reset();

	KeyModifiers amecs$getKeyModifiers();

	Text amecs$unmodified_getBoundKeyLocalizedText();
}
