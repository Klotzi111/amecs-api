package de.siphalor.amecs.impl;

import de.klotzi111.fabricmultiversionhelper.api.text.IMutableText;
import de.klotzi111.fabricmultiversionhelper.api.text.TextWrapper;
import de.siphalor.amecs.api.KeyModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModifierPrefixTextProvider {
	private static final Text SUFFIX = TextWrapper.literal(" + ");
	private static final Text COMPRESSED_SUFFIX = TextWrapper.literal("+");
	private final String translationKey;

	public ModifierPrefixTextProvider(KeyModifier modifier) {
		this(modifier.getTranslationKey());
	}

	public ModifierPrefixTextProvider(String translationKey) {
		this.translationKey = translationKey;
	}

	protected Text getBaseText(Variation variation) {
		return variation.getTranslatableText(translationKey);
	}

	public Text getText(Variation variation) {
		IMutableText text = (IMutableText) getBaseText(variation);
		if (variation == Variation.COMPRESSED) {
			text.fmvh$append(COMPRESSED_SUFFIX);
		} else {
			text.fmvh$append(SUFFIX);
		}
		return text;
	}

	public static enum Variation {
		COMPRESSED(".tiny"),
		TINY(".tiny"),
		SHORT(".short"),
		NORMAL("");

		// using this array for the values because it is faster than calling values() every time
		public static final Variation[] VALUES = Variation.values();

		public static final Variation WIDEST = NORMAL;
		public static final Variation SMALLEST = COMPRESSED;

		public final String translateKeySuffix;

		private Variation(String translateKeySuffix) {
			this.translateKeySuffix = translateKeySuffix;
		}

		public Text getTranslatableText(String translationKey) {
			return TextWrapper.translatable(translationKey + translateKeySuffix);
		}

		public Variation getNextVariation(int amount) {
			int targetOrdinal = ordinal() + amount;
			if (targetOrdinal < 0 || targetOrdinal >= VALUES.length) {
				return null;
			}
			return VALUES[targetOrdinal];
		}

		public Variation getSmaller() {
			return getNextVariation(-1);
		}
	}
}
