package de.siphalor.amecs.impl.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import de.klotzi111.fabricmultiversionhelper.api.mapping.MappingHelper;
import de.klotzi111.fabricmultiversionhelper.api.mixinselect.MixinSelectConfig;
import de.klotzi111.fabricmultiversionhelper.impl.mixinselect.ModVersionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;

@Environment(EnvType.CLIENT)
public class AmecsAPIMixinConfig implements IMixinConfigPlugin {

	// we can NOT use MOD_ID field because that would cause all statically class references in that class to be loaded to early
	private static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer("amecsapi").get();

	private static final String MOUSE_CLASS_INTERMEDIARY = "net.minecraft.class_312";
	private static final String SCREEN_CLASS_INTERMEDIARY = "net.minecraft.class_437";
	private static final String ELEMENT_CLASS_INTERMEDIARY = "net.minecraft.class_364";

	private String mouseClassRemapped;
	private String screenClassRemappedType;
	private String screenMouseScrolledRemapped;
	private String onMouseScrollRemapped;

	private List<String> mixinClasses = null;

	@Override
	public void onLoad(String mixinPackage) {
		// we can NOT use .class on the classes because that would trigger them to load and we are not allowed to load classes that early
		mouseClassRemapped = MappingHelper.CLASS_MAPPER_FUNCTION.apply(MOUSE_CLASS_INTERMEDIARY);
		screenClassRemappedType = MappingHelper.CLASS_MAPPER_FUNCTION.apply(SCREEN_CLASS_INTERMEDIARY).replace('.', '/');
		screenMouseScrolledRemapped = MappingHelper.MAPPING_RESOLVER.mapMethodName(MappingHelper.NAMESPACE_INTERMEDIARY, ELEMENT_CLASS_INTERMEDIARY, "method_25401", "(DDD)Z");
		onMouseScrollRemapped = MappingHelper.MAPPING_RESOLVER.mapMethodName(MappingHelper.NAMESPACE_INTERMEDIARY, MOUSE_CLASS_INTERMEDIARY, "method_1598", "(JDD)V");

		MixinSelectConfig selectConfig = MixinSelectConfig.loadMixinSelectConfig(MOD_CONTAINER);
		HashMap<String, Version> modsWithVersion = ModVersionHelper.getAllModsWithVersion(FabricLoader.getInstance(), true);
		mixinClasses = selectConfig.getAllowedMixins(mixinPackage, this.getClass().getClassLoader(), modsWithVersion);
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
		return mixinClasses == null ? null : (mixinClasses.isEmpty() ? null : mixinClasses);
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
