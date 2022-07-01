package de.siphalor.amecs.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.amecs.impl.compat.nmuk.CompatibilityNMUK;
import de.siphalor.amecs.impl.compat.nmuk.NMUKProxy;
import de.siphalor.amecs.impl.duck.IKeyBinding;
import de.siphalor.amecs.impl.mixin.KeyBindingAccessor;
import de.siphalor.amecs.impl.util.IdentityHashStrategy;
import de.siphalor.amecs.impl.version.KeyBindingVersionHelper;
import de.siphalor.amecs.impl.version.MinecraftClientVersionHelper;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;

@Environment(EnvType.CLIENT)
public class KeyBindingManager {
	// split it in two maps because it is ways faster to only stream the map with the objects we need
	// rather than streaming all and throwing out a bunch every time
	public static Map<Key, ObjectLinkedOpenCustomHashSet<KeyBinding>> KEYBINDINGS_BY_KEY = new HashMap<>();
	// TODO: These priority keyBindings are legacy issues. We can hopefully remove them in the future
	public static Map<Key, ObjectLinkedOpenCustomHashSet<KeyBinding>> KEYBINDINGS_BY_KEY_PRIORITY = new HashMap<>();

	// This EnumMap might bring problems with late injected additional enum values. We will see
	public static EnumMap<KeyModifier, ObjectLinkedOpenCustomHashSet<KeyBinding>> KEYBINDINGS_BY_KEYMODIFIER = new EnumMap<>(KeyModifier.class);

	/**
	 *
	 * @param keysById_map
	 * @param keyBinding
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	private static boolean removeKeyBindingFromListFromMap(Map<Key, ObjectLinkedOpenCustomHashSet<KeyBinding>> keysById_map, KeyBinding keyBinding) {
		// we need to get the backing list to remove elements thus we can not use any of the other methods that return streams
		Key keyCode = ((IKeyBinding) keyBinding).amecs$getBoundKey();
		ObjectLinkedOpenCustomHashSet<KeyBinding> keyBindings = keysById_map.get(keyCode);
		boolean ret = false;
		if (keyBindings != null) {
			// returns true if the keyBinding could be removed
			ret = keyBindings.remove(keyBinding);
		}

		// also remove the keyBinding from the keyModifier map
		removeKeyBindingWithKeyModifiers(keyBinding, false);

		return ret;
	}

	private static void removeKeyBindingWithKeyModifiers(KeyBinding keyBinding, boolean removeFromAll) {
		forEachKeyModifierInKeyBinding(keyBinding, removeFromAll, km -> {
			ObjectLinkedOpenCustomHashSet<KeyBinding> keyModifierKeyBindings = KEYBINDINGS_BY_KEYMODIFIER.get(km);
			if (keyModifierKeyBindings != null) {
				keyModifierKeyBindings.remove(keyBinding);
			}
		});
		// System.out.println("removeKB: now: " + keyBinding + "(" + keyBinding.getTranslationKey() + ") all: " + KEYBINDINGS_BY_KEYMODIFIER);
	}

	/**
	 *
	 * @param keysById_map
	 * @param keyBinding
	 * @return whether the keyBinding was added. It is not added if it is already contained
	 */
	private static boolean addKeyBindingToListFromMap(Map<Key, ObjectLinkedOpenCustomHashSet<KeyBinding>> keysById_map, KeyBinding keyBinding) {
		Key keyCode = ((IKeyBinding) keyBinding).amecs$getBoundKey();
		ObjectLinkedOpenCustomHashSet<KeyBinding> keyBindings = keysById_map.get(keyCode);
		if (keyBindings == null) {
			// we want them always in them same order so we need LinkedHashSet
			keyBindings = new ObjectLinkedOpenCustomHashSet<>(IdentityHashStrategy.IDENTITY_HASH_STRATEGY);
			keysById_map.put(keyCode, keyBindings);
		}
		// its a set it will return false if the keyBinding was already contained
		boolean ret = keyBindings.add(keyBinding);

		// also add the keyBinding to the keyModifier map
		addKeyBindingWithKeyModifiers(keyBinding);

		return ret;
	}

	private static void addKeyBindingWithKeyModifiers(KeyBinding keyBinding) {
		forEachKeyModifierInKeyBinding(keyBinding, false, km -> {
			ObjectLinkedOpenCustomHashSet<KeyBinding> keyModifierKeyBindings = KEYBINDINGS_BY_KEYMODIFIER.get(km);
			if (keyModifierKeyBindings == null) {
				// we want them always in them same order so we need LinkedHashSet
				keyModifierKeyBindings = new ObjectLinkedOpenCustomHashSet<>(IdentityHashStrategy.IDENTITY_HASH_STRATEGY);
				KEYBINDINGS_BY_KEYMODIFIER.put(km, keyModifierKeyBindings);
			}
			keyModifierKeyBindings.add(keyBinding);
		});
		// System.out.println("addKB: now: " + keyBinding + "(" + keyBinding.getTranslationKey() + ") all: " + KEYBINDINGS_BY_KEYMODIFIER);
	}

	private static void forEachKeyModifierInKeyBinding(KeyBinding keyBinding, boolean overrideIsSet, Consumer<KeyModifier> consumer) {
		KeyModifiers keyModifiers = ((IKeyBinding) keyBinding).amecs$getKeyModifiers();
		for (KeyModifier km : KeyModifier.VALUES) {
			if (km == KeyModifier.NONE) {
				continue;
			}
			if (overrideIsSet || keyModifiers.get(km)) {
				consumer.accept(km);
			}
		}
	}

	private static Stream<KeyBinding> getKeyBindingsFromMap(Map<Key, ObjectLinkedOpenCustomHashSet<KeyBinding>> keysById_map) {
		return keysById_map.values().stream().flatMap(Collection::stream);
	}

	private static void forEachKeyBinding(Consumer<KeyBinding> consumer) {
		getKeyBindingsFromMap(KEYBINDINGS_BY_KEY_PRIORITY).forEach(consumer);
		getKeyBindingsFromMap(KEYBINDINGS_BY_KEY).forEach(consumer);
	}

	public static Collector<KeyBinding, ?, ObjectLinkedOpenCustomHashSet<KeyBinding>> IDENTITY_LINKED_HASH_SET_COLLECTOR = Collectors
		.toCollection(() -> new ObjectLinkedOpenCustomHashSet<>(IdentityHashStrategy.IDENTITY_HASH_STRATEGY));

	public static ObjectLinkedOpenCustomHashSet<KeyBinding> getMatchingKeyBindings(Key keyCode, boolean priority) {
		ObjectLinkedOpenCustomHashSet<KeyBinding> keyBindingList = (priority ? KEYBINDINGS_BY_KEY_PRIORITY : KEYBINDINGS_BY_KEY).get(keyCode);
		if (keyBindingList == null) {
			return new ObjectLinkedOpenCustomHashSet<>(0, IdentityHashStrategy.IDENTITY_HASH_STRATEGY);
		}
		// The code below should create new instances of the set. This is done here via streaming and collecting

		// this looks not right: If you have a kb: alt + y and shift + alt + y and you press shift + alt + y both will be triggered
		// Correction: It works as it should. Leaving this comments for future readers
		Stream<KeyBinding> result = keyBindingList.stream().filter(keyBinding -> ((IKeyBinding) keyBinding).amecs$getKeyModifiers().isPressed());
		ObjectLinkedOpenCustomHashSet<KeyBinding> keyBindings = result.collect(IDENTITY_LINKED_HASH_SET_COLLECTOR);
		if (keyBindings.isEmpty()) {
			return keyBindingList.stream().filter(keyBinding -> ((IKeyBinding) keyBinding).amecs$getKeyModifiers().isUnset()).collect(IDENTITY_LINKED_HASH_SET_COLLECTOR);
		}
		return keyBindings;
	}

	public static ObjectLinkedOpenCustomHashSet<KeyBinding> getKeyBindingsWithKey(Key key) {
		ObjectLinkedOpenCustomHashSet<KeyBinding> set = getMatchingKeyBindings(key, true);
		set.addAll(getMatchingKeyBindings(key, false));
		return set;
	}

	/**
	 *
	 * @param keyModifier
	 * @return The raw Set instance of the map if non null. Be careful
	 */
	public static ObjectLinkedOpenCustomHashSet<KeyBinding> getKeyBindingsWithKeyModifier(KeyModifier keyModifier) {
		ObjectLinkedOpenCustomHashSet<KeyBinding> keyBindings = KEYBINDINGS_BY_KEYMODIFIER.get(keyModifier);
		if (keyBindings == null) {
			return new ObjectLinkedOpenCustomHashSet<>(0, IdentityHashStrategy.IDENTITY_HASH_STRATEGY);
		}
		return keyBindings;
		// If we would want to make a safety copy. But we also want performance and since this class in declared IMPLEMENTATION we should be fine
		// return new ObjectLinkedOpenCustomHashSet<>(keyBindings, IdentityHashStrategy.IDENTITY_HASH_STRATEGY);
	}

	public static void updateKeyBindingWithKeyModifiers(KeyBinding keyBinding) {
		removeKeyBindingWithKeyModifiers(keyBinding, true);
		addKeyBindingWithKeyModifiers(keyBinding);
	}

	/**
	 *
	 * @param keyBinding
	 * @return whether the keyBinding was added. It is not added if it is already contained
	 */
	public static boolean register(KeyBinding keyBinding) {
		if (keyBinding instanceof PriorityKeyBinding) {
			return addKeyBindingToListFromMap(KEYBINDINGS_BY_KEY_PRIORITY, keyBinding);
		} else {
			return addKeyBindingToListFromMap(KEYBINDINGS_BY_KEY, keyBinding);
		}
	}

	/**
	 *
	 * @param keyBinding
	 * @return whether the keyBinding was removed. It is not removed if it was not contained
	 */
	public static boolean unregister(KeyBinding keyBinding) {
		if (keyBinding == null) {
			return false;
		}
		// do not rebuild the entire map if we do not have to
		// KeyBinding.updateKeysByCode();
		// instead
		boolean removed = false;
		removed |= removeKeyBindingFromListFromMap(KEYBINDINGS_BY_KEY, keyBinding);
		removed |= removeKeyBindingFromListFromMap(KEYBINDINGS_BY_KEY_PRIORITY, keyBinding);
		return removed;
	}

	public static void updateKeysByCode() {
		KEYBINDINGS_BY_KEY.clear();
		KEYBINDINGS_BY_KEY_PRIORITY.clear();
		KEYBINDINGS_BY_KEYMODIFIER.clear();
		KeyBindingAccessor.getKeysById().values().forEach(KeyBindingManager::register);
	}

	public static void unpressAll() {
		KeyBindingAccessor.getKeysById().values().forEach(keyBinding -> ((IKeyBinding) keyBinding).amecs$reset());
	}

	public static void onKeyPressed(Key keyCode) {
		getMatchingKeyBindings(keyCode, false).forEach(keyBinding -> {
			((IKeyBinding) keyBinding).amecs$incrementTimesPressed();

			// It is not that clean to have the logic of NMUK here as well
			// but we definitely need to do that because we are not calling the 'onKeyPressed' method of the keyBinding
			if (CompatibilityNMUK.MOD_PRESENT) {
				KeyBinding parent = NMUKProxy.getParent(keyBinding);
				if (parent != null) {
					((IKeyBinding) parent).amecs$incrementTimesPressed();
				}
			}

		});
	}

	// TODO: Do these PriorityKeyBindings still work? Do they also need to get called in onKeyPressed? or are they?
	public static boolean onKeyPressedPriority(Key keyCode) {
		// because streams do evaluation lazy this code does only call onPressedPriority on so many keyBinding until one returns true
		// Or if no one returns true all are called and an empty optional is returned
		Optional<KeyBinding> keyBindings = getMatchingKeyBindings(keyCode, true).stream().filter(keyBinding -> ((PriorityKeyBinding) keyBinding).onPressedPriority()).findFirst();
		return keyBindings.isPresent();
	}

	public static void updatePressedStates() {
		@SuppressWarnings("resource")
		long windowHandle = MinecraftClientVersionHelper.getWindow(MinecraftClient.getInstance()).getHandle();
		forEachKeyBinding(keyBinding -> {
			Key key = ((IKeyBinding) keyBinding).amecs$getBoundKey();
			boolean pressed = !keyBinding.isUnbound() && key.getCategory() == InputUtil.Type.KEYSYM && InputUtil.isKeyPressed(windowHandle, key.getCode());
			KeyBindingVersionHelper.setPressed(keyBinding, pressed);
		});
	}

	// used by KTIG
	public static void checkKeyBindingsWithJustReleasedKeyModifier(Key keyCode, boolean pressed, Function<Collection<KeyBinding>, Stream<KeyBinding>> createFilteredStreamFunction) {
		KeyModifier keyModifier = KeyModifier.fromKeyCode(keyCode.getCode());
		if (keyModifier != KeyModifier.NONE) {
			// if it is a keyModifier
			if (!pressed) {
				// and it was just released
				// we need to search keyBindinds that require the keyModifier
				// and disable them
				createFilteredStreamFunction.apply(getKeyBindingsWithKeyModifier(keyModifier)).forEach(keyBinding -> {
					KeyBindingVersionHelper.setPressed(keyBinding, false);
					// System.out.println("keyMod disabled: " + ((IKeyBinding) keyBinding).amecs$getBoundKey());
				});

			}
		}
	}

	public static void setKeyPressed(Key keyCode, boolean pressed) {
		// System.out.println("setKeyPressed: " + keyCode.getCode() + ", " + pressed);

		checkKeyBindingsWithJustReleasedKeyModifier(keyCode, pressed, (kbs) -> kbs.stream());

		// This is done in MixinKeyboard#onKey
		//// AmecsAPI.CURRENT_MODIFIERS.set(keyModifier, pressed);

		getKeyBindingsWithKey(keyCode).forEach(keyBinding -> KeyBindingVersionHelper.setPressed(keyBinding, pressed));
	}
}
