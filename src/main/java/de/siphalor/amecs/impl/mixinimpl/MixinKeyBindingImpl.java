package de.siphalor.amecs.impl.mixinimpl;

import java.util.Objects;

import org.lwjgl.glfw.GLFW;

import de.klotzi111.fabricmultiversionhelper.api.text.IMutableText;
import de.klotzi111.fabricmultiversionhelper.api.text.TextRendererWrapper;
import de.klotzi111.fabricmultiversionhelper.api.text.TextWrapper;
import de.klotzi111.fabricmultiversionhelper.api.version.MinecraftVersionHelper;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.impl.ModifierPrefixTextProvider;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.text.Text;

public class MixinKeyBindingImpl {

	public static Text getKeyLocalizedTextBefore1_16(Key key) {
		String translationKey = key.getTranslationKey();
		int i = key.getCode();
		String ret = null;
		switch (key.getCategory()) {
			case KEYSYM: {
				ret = GLFW.glfwGetKeyName(i, -1);
				break;
			}
			case SCANCODE: {
				ret = GLFW.glfwGetKeyName(-1, i);
				break;
			}
			case MOUSE: {
				String translated = I18n.translate(translationKey);
				if (Objects.equals(translated, translationKey)) {
					return TextWrapper.translatable("key.mouse", i + 1);
				} else {
					return TextWrapper.translatable(translationKey);
				}
			}
		}
		return ret == null ? TextWrapper.translatable(translationKey) : TextWrapper.literal(ret);
	}

	public static Text getBoundKeyLocalizedText(IKeyBinding keyBinding) {
		if (MinecraftVersionHelper.isMCVersionAtLeast("1.16")) {
			return keyBinding.amecs$getBoundKey().getLocalizedText();
		} else {
			return getKeyLocalizedTextBefore1_16(keyBinding.amecs$getBoundKey());
		}
	}

	public static Text getModifierCompresedBoundKeyLocalizedText(IKeyBinding keyBinding) {
		Text name = getBoundKeyLocalizedText(keyBinding);
		Text fullName;
		@SuppressWarnings("resource")
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		ModifierPrefixTextProvider.Variation variation = ModifierPrefixTextProvider.Variation.WIDEST;
		do {
			fullName = name;
			for (KeyModifier keyModifier : KeyModifier.VALUES) {
				if (keyModifier == KeyModifier.NONE) {
					continue;
				}

				if (keyBinding.amecs$getKeyModifiers().get(keyModifier)) {
					Text baseText = keyModifier.textProvider.getText(variation);
					fullName = ((IMutableText) baseText).fmvh$append(fullName);
				}
			}
		} while ((variation = variation.getSmaller()) != null && TextRendererWrapper.getWidth(textRenderer, fullName) > 70);

		return fullName;
	}

}
