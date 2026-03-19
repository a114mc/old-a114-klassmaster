// Header added by a114mc
/*
 * Scuti
 * https://github.com/netindev/scuti
 * Licensed under WTFPL
 */

package me.iris.ambien.obfuscator.transformers.impl.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.kek.myj2c.Myj2cASMUtils;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 * @author netindev
 *
 */
@TransformerInfo(
        name = "invoke-dynamic",
        description = "Invoke dynamically",
        category = Category.CONTROL_FLOW,
        stability = Stability.STABLE,
        ordinal = Ordinal.LOW
)
public class InvokeDynamicTransformer extends Transformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeDynamicTransformer.class);
    private final AtomicInteger atomicInteger = new AtomicInteger();

//    public InvokeDynamicTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
//                                    final Map<String, ClassNode> dependencies) {
//        super(configuration, classes, dependencies);
//        LOGGER.info(" Invoke Dynamic Transformer ->");
//    }

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper).stream()
                .filter(cw -> Myj2cASMUtils.classNodeIsNotInterface(cw.getNode()) && cw.getNode().version >= Opcodes.V1_7)
                .forEach(classNode -> {
                    final String bootstrapName = StringUtil.genName(3);

                    if (this.insertDynamic(classNode.getNode(), bootstrapName)) {
                        classNode.getNode().methods.add(this.createBootstrap(classNode.getNode().name, bootstrapName));
                    }
                });
        LOGGER.info(" - Replaced " + this.atomicInteger.get() + " calls");
    }

    private boolean insertDynamic(final ClassNode classNode, final String bootstrapName) {
        boolean b = false;
        for (MethodNode methodNode : classNode.methods) {
            if (!Modifier.isAbstract(methodNode.access)) {
                for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if ((insnNode instanceof MethodInsnNode || insnNode instanceof FieldInsnNode)
                            && insnNode.getOpcode() != Opcodes.INVOKESPECIAL)
                    {
                        if (insnNode instanceof MethodInsnNode) {
                            final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;

                            if(Ambien.exclusionManager.isClassExcluded("invoke-dynamic", classNode)){
                                break;
                            }
                            if(Ambien.exclusionManager.isMethodExcluded(classNode.name, methodInsnNode.name)){
                                break;
                            }

                            final Handle handle = new Handle(Opcodes.H_INVOKESTATIC, classNode.name, bootstrapName,
                                    MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
                                                    MethodType.class, String.class, String.class, String.class, Integer.class)
                                            .toMethodDescriptorString(),
                                    false);
                            // Lookup logic might be bad
                            final InvokeDynamicInsnNode invokeDynamicInsnNode = new InvokeDynamicInsnNode(
                                    StringUtil.randomStringIS(1,"abcdefghijklmnopqrstuvwxyz"),
                                    methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC ? methodInsnNode.desc
                                            : methodInsnNode.desc.replace("(", "(Ljava/lang/Object;"),
                                    handle, methodInsnNode.owner.replace("/", "."),
                                    methodInsnNode.name,
                                    methodInsnNode.desc,
                                    methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC ? 0 : 1);
                            methodNode.instructions.insert(insnNode, invokeDynamicInsnNode);
                            methodNode.instructions.remove(insnNode);
                            b = true;
                            this.atomicInteger.incrementAndGet();
                        }
                    }
                }
            }
        }
        return b;
    }

    private MethodNode createBootstrap(final String className, final String methodName) {
        final MethodNode methodNode = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName,
                MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class,
                        String.class, String.class, String.class, Integer.class).toMethodDescriptorString(),
                null, null);
        methodNode.visitCode();
        final Label firstLabel = new Label();
        final Label secondLabel = new Label();
        final Label thirthLabel = new Label();
        methodNode.visitTryCatchBlock(firstLabel, secondLabel, thirthLabel, "java/lang/Exception");
        final Label fourthLabel = new Label();
        final Label fifthLabel = new Label();
        methodNode.visitTryCatchBlock(fourthLabel, fifthLabel, thirthLabel, "java/lang/Exception");
        methodNode.visitVarInsn(Opcodes.ALOAD, 3);
        methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
        methodNode.visitVarInsn(Opcodes.ASTORE, 7);
        methodNode.visitVarInsn(Opcodes.ALOAD, 4);
        methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
        methodNode.visitVarInsn(Opcodes.ASTORE, 8);
        methodNode.visitVarInsn(Opcodes.ALOAD, 5);
        methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
        methodNode.visitVarInsn(Opcodes.ASTORE, 9);
        methodNode.visitVarInsn(Opcodes.ALOAD, 6);
        methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
        methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        methodNode.visitVarInsn(Opcodes.ISTORE, 10);
        methodNode.visitVarInsn(Opcodes.ALOAD, 9);

        methodNode.visitLdcInsn(Type.getType("L" + className + ";"));
        methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getClassLoader",
                "()Ljava/lang/ClassLoader;", false);
        methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString",
                "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
        methodNode.visitVarInsn(Opcodes.ASTORE, 11);
        methodNode.visitLabel(firstLabel);
        methodNode.visitVarInsn(Opcodes.ILOAD, 10);
        methodNode.visitInsn(Opcodes.ICONST_1);
        methodNode.visitJumpInsn(Opcodes.IF_ICMPNE, fourthLabel);
        methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
        methodNode.visitInsn(Opcodes.DUP);
        methodNode.visitVarInsn(Opcodes.ALOAD, 0);
        methodNode.visitVarInsn(Opcodes.ALOAD, 7);

        methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
                "(Ljava/lang/String;)Ljava/lang/Class;", false);
        methodNode.visitVarInsn(Opcodes.ALOAD, 8);

        methodNode.visitVarInsn(Opcodes.ALOAD, 11);
        methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findVirtual",
                "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
                false);
        methodNode.visitVarInsn(Opcodes.ALOAD, 2);
        methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType",
                "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
        methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>",
                "(Ljava/lang/invoke/MethodHandle;)V", false);
        methodNode.visitLabel(secondLabel);
        methodNode.visitInsn(Opcodes.ARETURN);
        methodNode.visitLabel(fourthLabel);
        methodNode.visitFrame(Opcodes.F_FULL, 12,
                new Object[] { "java/lang/invoke/MethodHandles$Lookup", "java/lang/String",
                        "java/lang/invoke/MethodType", "java/lang/Object", "java/lang/Object", "java/lang/Object",
                        "java/lang/Object", "java/lang/String", "java/lang/String", "java/lang/String", Opcodes.INTEGER,
                        "java/lang/invoke/MethodType" },
                0, new Object[] {});
        methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
        methodNode.visitInsn(Opcodes.DUP);
        methodNode.visitVarInsn(Opcodes.ALOAD, 0);
        methodNode.visitVarInsn(Opcodes.ALOAD, 7);

        methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
                "(Ljava/lang/String;)Ljava/lang/Class;", false);
        methodNode.visitVarInsn(Opcodes.ALOAD, 8);

        methodNode.visitVarInsn(Opcodes.ALOAD, 11);
        methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic",
                "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
                false);
        methodNode.visitVarInsn(Opcodes.ALOAD, 2);
        methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType",
                "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
        methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>",
                "(Ljava/lang/invoke/MethodHandle;)V", false);
        methodNode.visitLabel(fifthLabel);
        methodNode.visitInsn(Opcodes.ARETURN);
        methodNode.visitLabel(thirthLabel);
        methodNode.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Exception" });
        methodNode.visitVarInsn(Opcodes.ASTORE, 12);
        methodNode.visitInsn(Opcodes.ACONST_NULL);
        methodNode.visitInsn(Opcodes.ARETURN);
        methodNode.visitMaxs(0, 0);
        methodNode.visitEnd();
        return methodNode;
    }
}
