package de.siphalor.amecs.impl.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import de.siphalor.amecs.impl.compat.controlling.CompatibilityControlling;
import de.siphalor.amecs.impl.compat.nmuk.CompatibilityNMUK;
import de.siphalor.amecs.impl.version.MinecraftVersionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

@Environment(EnvType.CLIENT)
public class AmecsAPIMixinConfig implements IMixinConfigPlugin {

	public static final String MIXIN_VERSIONED_PACKAGE = "versioned";

	public static String prependMixinPackage(String className, String prefix) {
		if (prefix == null) {
			return className;
		}
		return prefix + "." + className;
	}

	public static List<String> prependMixinPackages(List<String> classNames, String prefix) {
		List<String> ret = new ArrayList<>(classNames.size());
		for (String className : classNames) {
			ret.add(prependMixinPackage(className, prefix));
		}
		return ret;
	}

	private List<String> finalAdditionalMixinClasses = new ArrayList<>();

	private List<String> additionalMixinClasses = new ArrayList<>();

	private void addMixins(String... mixinNames) {
		Collections.addAll(additionalMixinClasses, mixinNames);
	}

	private void pushMixinsToFinal() {
		finalAdditionalMixinClasses.addAll(additionalMixinClasses);
		additionalMixinClasses.clear();
	}

	private static final String MOUSE_CLASS_INTERMEDIARY = "net.minecraft.class_312";
	private static final String SCREEN_CLASS_INTERMEDIARY = "net.minecraft.class_437";
	private static final String ELEMENT_CLASS_INTERMEDIARY = "net.minecraft.class_364";

	private final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

	private String mouseClassRemapped;
	private String screenClassRemappedType;
	private String screenMouseScrolledRemapped;
	private String onMouseScrollRemapped;

	@Override
	public void onLoad(String mixinPackage) {
		mouseClassRemapped = mappingResolver.mapClassName("intermediary", MOUSE_CLASS_INTERMEDIARY);
		screenClassRemappedType = mappingResolver.mapClassName("intermediary", SCREEN_CLASS_INTERMEDIARY).replace('.', '/');
		screenMouseScrolledRemapped = mappingResolver.mapMethodName("intermediary", ELEMENT_CLASS_INTERMEDIARY, "method_25401", "(DDD)Z");
		onMouseScrollRemapped = mappingResolver.mapMethodName("intermediary", MOUSE_CLASS_INTERMEDIARY, "method_1598", "(JDD)V");

		// versioned mixins

		// TODO: add a json config file where for each mixinClassName a modID requirement can be made. Like in the fabric.mod.json#depends.
		// for now doing it in here

		// the order of the if statements is important. The highest version must be checked first
		if (MinecraftVersionHelper.IS_AT_LEAST_V1_18) {
			addMixins("MixinKeybindsScreen");
		} else {
			// Minecraft 1.17 and below
			addMixins("MixinControlsOptionsScreen");
		}

		additionalMixinClasses = prependMixinPackages(additionalMixinClasses, MIXIN_VERSIONED_PACKAGE);
		pushMixinsToFinal();

		if (CompatibilityControlling.MOD_PRESENT) {
			addMixins("MixinKeyEntry", "MixinNewKeyBindsScreen");

			additionalMixinClasses = prependMixinPackages(additionalMixinClasses, CompatibilityControlling.MOD_NAME);
			pushMixinsToFinal();
		}

		if (CompatibilityNMUK.MOD_PRESENT) {
			addMixins("MixinNMUKKeyBindingHelper");

			additionalMixinClasses = prependMixinPackages(additionalMixinClasses, CompatibilityNMUK.MOD_NAME);
			pushMixinsToFinal();
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return finalAdditionalMixinClasses == null ? null : (finalAdditionalMixinClasses.isEmpty() ? null : finalAdditionalMixinClasses);
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if (targetClassName.equals(mouseClassRemapped)) {
			for (MethodNode method : targetClass.methods) {
				if (onMouseScrollRemapped.equals(method.name)) {
					// if we found the matching method we replace it
					targetClass.methods.remove(method);
					method.accept(new OnMouseScrollTransformer(
						targetClass.visitMethod(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0])),
						method.access, method.name, method.desc));

					break;
				}
			}
		}
	}

	// The purpose of this is to capture the return value of the currentScreen.mouseScrolled call.
	// Other mods also require this so this would lead to @Redirect conflicts when done via mixins.
	private class OnMouseScrollTransformer extends GeneratorAdapter {
		protected OnMouseScrollTransformer(MethodVisitor methodVisitor, int access, String name, String descriptor) {
			super(Opcodes.ASM9, methodVisitor, access, name, descriptor);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			if (opcode == Opcodes.INVOKEVIRTUAL && screenClassRemappedType.equals(owner) && screenMouseScrolledRemapped.equals(name)) {
				super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
				super.loadThis();
				super.dupX1();
				super.pop();
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, mouseClassRemapped.replace('.', '/'), "amecs$onMouseScrolledScreen", "(Z)Z", false);
				return;
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}
	}

}
