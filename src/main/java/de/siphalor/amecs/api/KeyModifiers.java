package de.siphalor.amecs.api;

import java.util.Arrays;
import java.util.BitSet;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import de.siphalor.amecs.impl.AmecsAPI;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Defines modifiers for a key binding
 */
@Environment(EnvType.CLIENT)
public class KeyModifiers {
	/**
	 * This field is for comparison ONLY.
	 * <p>
	 * Trying to change the modifiers of it will fail with an {@link UnsupportedOperationException}
	 */
	public static final KeyModifiers NO_MODIFIERS = new FinalKeyModifiers();

	private static class FinalKeyModifiers extends KeyModifiers {
		private static final String EXCEPTION_MESSAGE = "You must not alter this Modifiers object";

		@Override
		public KeyModifiers setValue(boolean[] value) {
			throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
		}

		@Override
		public void set(KeyModifier keyModifier, boolean value) {
			throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
		}

		@Override
		public void unset() {
			throw new UnsupportedOperationException(EXCEPTION_MESSAGE);
		}
	}

	// using a boolean array here because it is faster and needs less space
	private final boolean[] value;

	/**
	 * Constructs new object with no modifiers set
	 */
	public KeyModifiers() {
		this(new boolean[KeyModifier.getModifierCount()]);
	}

	/**
	 * FOR INTERNAL USE ONLY
	 * <p>
	 * Constructs a new modifier object by a raw {@link BitSet}
	 *
	 * @param value
	 *        the raw value with flags set
	 */
	@ApiStatus.Internal
	public KeyModifiers(boolean[] value) {
		if (value.length != KeyModifier.getModifierCount()) {
			throw new IllegalArgumentException("value.length != KeyModifier.getModifierCount(): " + KeyModifier.getModifierCount());
		}
		this.value = value;
	}

	/**
	 * Constructs a new modifier object by all modifier bits
	 *
	 * @param alt sets whether the alt flag should be set
	 * @param control sets whether the control flag should be set
	 * @param shift sets whether the shift flag should be set
	 */
	public KeyModifiers(boolean alt, boolean control, boolean shift) {
		this();
		setAlt(alt);
		setControl(control);
		setShift(shift);
	}

	/**
	 * Compares this object with the currently pressed keys
	 *
	 * @return whether the modifiers match in the current context
	 */
	public boolean isPressed() {
		return equals(AmecsAPI.CURRENT_MODIFIERS);
	}

	/**
	 * FOR INTERNAL USE ONLY
	 * <p>
	 * Sets the raw value
	 *
	 * @param value the value with flags set
	 * @return this
	 */
	@ApiStatus.Internal
	public KeyModifiers setValue(boolean[] value) {
		int length = this.value.length;
		if (value.length != length) {
			throw new IllegalArgumentException("value != this.value.length: " + length);
		}
		System.arraycopy(value, 0, this.value, 0, length);
		return this;
	}

	/**
	 * FOR INTERNAL USE ONLY
	 * <p>
	 * Gets the raw value
	 *
	 * @return the value with all flags set
	 */
	@ApiStatus.Internal
	public boolean[] getValue() {
		return value;
	}

	/**
	 * FOR INTERNAL USE ONLY
	 * <p>
	 * copies the modifiers of the other KeyModifiers object into this
	 *
	 * @param other the modifiers to copy
	 */
	@ApiStatus.Internal
	public void copyModifiers(KeyModifiers other) {
		setValue(other.getValue());
	}

	/**
	 * Sets the alt flag
	 *
	 * @param value whether the alt flag should be activated or not
	 * @return this
	 */
	public KeyModifiers setAlt(boolean value) {
		set(KeyModifier.ALT, value);
		return this;
	}

	/**
	 * Gets the state of the alt flag
	 *
	 * @return whether the alt key needs to be pressed
	 */
	public boolean getAlt() {
		return get(KeyModifier.ALT);
	}

	/**
	 * Sets the control flag
	 *
	 * @param value whether the control flag should be activated or not
	 * @return this
	 */
	public KeyModifiers setControl(boolean value) {
		set(KeyModifier.CONTROL, value);
		return this;
	}

	/**
	 * Gets the state of the control flag
	 *
	 * @return whether the control key needs to be pressed
	 */
	public boolean getControl() {
		return get(KeyModifier.CONTROL);
	}

	/**
	 * Sets the shift flag
	 *
	 * @param value whether the shift flag should be activated or not
	 * @return this
	 */
	public KeyModifiers setShift(boolean value) {
		set(KeyModifier.SHIFT, value);
		return this;
	}

	/**
	 * Gets the state of the shift flag
	 *
	 * @return whether the shift key needs to be pressed
	 */
	public boolean getShift() {
		return get(KeyModifier.SHIFT);
	}

	/**
	 * Sets the given {@code keyModifier} flag to the given {@code value}
	 *
	 * @param keyModifier
	 *
	 * @param value whether the flag should be activated or not
	 */
	public void set(KeyModifier keyModifier, boolean value) {
		if (keyModifier != KeyModifier.NONE) {
			this.value[keyModifier.id] = value;
		}
	}

	/**
	 * Gets the given value for the given {@code keyModifier} flag
	 *
	 * @param keyModifier
	 *
	 * @param value whether the flag should be activated or not
	 * @return flag value
	 */
	public boolean get(KeyModifier keyModifier) {
		if (keyModifier == KeyModifier.NONE) {
			return true;
		}
		return value[keyModifier.id];
	}

	/**
	 * Returns whether no flag is set
	 *
	 * @return value == 0
	 */
	public boolean isUnset() {
		return !ArrayUtils.contains(value, true);
	}

	/**
	 * Clears all flags
	 */
	public void unset() {
		Arrays.fill(value, false);
	}

	/**
	 * Cleans up the flags by the key code present in the given key binding
	 *
	 * @param keyBinding the key binding from where to extract the key code
	 */
	public void cleanup(KeyBinding keyBinding) {
		InputUtil.Key key = ((IKeyBinding) keyBinding).amecs$getBoundKey();
		set(KeyModifier.fromKey(key), false);
	}

	/**
	 * Returns whether this object equals another one
	 *
	 * @param other another modifier object
	 * @return whether both values are equal
	 */
	public boolean equals(KeyModifiers other) {
		return Arrays.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "KeyModifiers [alt=" + getAlt() + ", control=" + getControl() + ", shift=" + getShift() + "]";
	}

	// new format even if it needs more characters because it is more user friendly (and simpler to parse). Not everyone knows about bit masks
	// it could be discussed whether this new is really "better" but i leave it for now. It is backward compatible so nothing breaks
	/**
	 * FOR INTERNAL USE ONLY
	 *
	 * @return the serialized string representation of the modifiers
	 */
	@ApiStatus.Internal
	public String serializeValue() {
		StringBuilder sb = new StringBuilder();
		for (boolean b : value) {
			sb.append(b ? 1 : 0);
			sb.append(",");
		}
		// remove trailing comma
		// this will fail if value.length is 0. But that would be useless anyways
		sb.setLength(sb.length() - ",".length());
		return sb.toString();
	}

	/**
	 * FOR INTERNAL USE ONLY
	 *
	 * @param value the serialized String value
	 *
	 * @return the deserialized modifier array
	 */
	@ApiStatus.Internal
	public static boolean[] deserializeValue(String value) {
		boolean[] ret = new boolean[KeyModifier.getModifierCount()];
		if (value.isEmpty()) {
			return ret;
		}
		// backward compatibility for old format
		if (!value.contains(",")) {
			// we never had more than one value with the fat long
			long packedModifiers = Long.parseLong(value, 16);
			for (KeyModifier keyModifier : KeyModifier.VALUES) {
				if (keyModifier == KeyModifier.NONE) {
					continue;
				}
				long mask = (1 << keyModifier.id);
				ret[keyModifier.id] = (packedModifiers & mask) == mask;
			}
			return ret;
		}
		// we have the new format
		int i = 0;
		for (String p : value.split(",")) {
			ret[i++] = p.equals("1");
		}
		return ret;
	}
}
