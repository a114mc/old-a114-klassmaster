package me.iris.ambien.obfuscator.transformers.impl.flow;

import me.iris.ambien.obfuscator.builders.InstructionModifier;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@TransformerInfo(
		name = "junk-code", category = Category.CONTROL_FLOW,
		stability = Stability.STABLE, ordinal = Ordinal.LOW,
		description = "Generates junk code."
)
public class JunkCode extends Transformer {
	private final Map<ClassWrapper, List<MethodWrapper>> classMethodsMap = new ConcurrentHashMap<>();

	@Override public void transform (JarWrapper wrapper) {
		getClasses(wrapper).stream()
				.filter(classWrapper -> !classWrapper.isInterface())
				.forEach(classWrapper -> {
					List<MethodWrapper> methods = classWrapper
							.getTransformableMethods()
							.stream()
							.filter(methodWrapper -> !GOTOASMUtils.isSpecialMethod(
									methodWrapper.getNode()) && !methodWrapper.isInitializer())
							.collect(Collectors.toList());
					
					classMethodsMap.put(classWrapper, methods);
				});

		classMethodsMap.forEach(
				(classWrapper, methods) -> methods.forEach(this::injectJunkCode));
	}

	/**
	 * Injects junk code into the given method.
	 * The junk code will never be executed, but it will make the method larger and more complex.
	 * Might against RE, who knows.
	 *
	 * @param methodWrapper The method wrapper to inject junk code into.
	 */
	public void injectJunkCode (MethodWrapper methodWrapper) {
		InstructionModifier modifier = new InstructionModifier();
		MethodNode method = methodWrapper.getNode();

		for (AbstractInsnNode instruction : methodWrapper.getInstructionsList()) {
			if (instruction instanceof LabelNode) {
                try {
                    if (method.instructions.indexOf(
                            instruction) == method.instructions.size() - 1) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }

                LabelNode label = new LabelNode();

				InsnList list = new InsnList();
				list.add(label);
				list.add(GOTOASMUtils.createNumberNode(ThreadLocalRandom.current()
						                          .nextInt(0, Integer.MAX_VALUE)));
				list.add(new JumpInsnNode(IFGE, (LabelNode) instruction));

				int sb = ThreadLocalRandom.current()
						.nextInt(Short.MIN_VALUE, 0);

				// sipush a negative number
				list.add(new IntInsnNode(GOTOASMUtils.getNumberOpcode(sb), sb));

				// negate the number
				list.add(new InsnNode(Opcodes.INEG));
				// System.exit (I)V call this

				// Removed array sht due to java.lang.ClassFormatError: Invalid
				// cunt: blah blah blah
				list.add(new MethodInsnNode(
						INVOKESTATIC, "java/lang/System", "exit",
				                            "(I)V", false
				));
				modifier.prepend(instruction, list);
			}
		}

		modifier.apply(method);

	}
}
