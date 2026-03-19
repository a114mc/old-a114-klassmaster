/*
 * a114-klassmaster
 * A modified bruhfuscator(modified ambien obfuscator)
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.transformers.impl.data.string;

import cn.a114.commonutil.random.ThreadLocalRandomManager;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.SizeEvaluator;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.transformers.impl.data.StringEncryptionManager;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.string.Namings;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;

/**
 * @author a114
 * <p>
 * Note: This string encryption <strong>looks like but not equals</strong> allatori's algorithm
 * as allatori only requires 1 string pass throuth thier method
 * </p>
 * <h1>Use after remap otherwise strings will got garbaged</h1>
 */
public class AllatoriLikeStringEncryption implements Opcodes {
    public static final String _String_int_returns_String_descriptor =
            // String int -> String
            "(Ljava/lang/String;I)Ljava/lang/String;", boomNodeName = "jiba/RandomShit", boomerName = "penis";


    public static final HashMap<Integer, Integer> keyStore = new HashMap<>();
    // Allatori doesn't have this shit
    public static String combiner = "-";
    public static String a_methodName = "会被替换掉的无意义的string";


    public static char min = '\u3040', max = '\u309f';

    private static boolean boom = StringEncryptionManager.boom.isEnabled();

    public static void process(ClassNode classNode) {

        boom = StringEncryptionManager.boom.isEnabled();
        pre:
        {
            if (!boom) {
                combiner = StringUtil.randomStringIS(1, StringUtil.randomStringByNaming(0xff, Namings.nonAscii));
                a_methodName = StringUtil.randomStringByNaming(ThreadLocalRandomManager
                                .theThreadLocalRandom
                                .nextInt(2,
                                        32),
                        Ambien.get.theNamingNaming
                );
            } else {
                a_methodName = boomerName;
                combiner = "-";
            }
        }
        boolean processed = false;

        for (MethodNode method : classNode.methods) {

            // Skip the decrypt method itself and empty methods
            if (method.name.equals(a_methodName)
                    || method.name.equals(DashOStringEncryption.d_methodName)

                    || GOTOASMUtils.isSpecialMethod(method)) {
                continue;
            }

            InsnList insns = method.instructions;
            if (SizeEvaluator.willOverflow(method, insns)) {
                continue;
            }

            for (AbstractInsnNode insn : insns.toArray()) { // Use toArray() to avoid concurrent modification
                if (insn instanceof LdcInsnNode) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (ldc.cst instanceof String) {
                        // Do not process empty string calls
                        if (((String) ldc.cst).isEmpty()) {
                            continue;
                        }
                        processed = true;
                        processStringConstant(classNode, method, insns, ldc);
                    }
                } else if (insn instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode invokeDynamic = (InvokeDynamicInsnNode) insn;
                    if (isStringConcatFactory(invokeDynamic)) {
                        processed = processInvokeDynamic(
                                classNode, method, insns,
                                invokeDynamic
                        );
                    }
                }
            }
        }

        post:
        {
            // 炸弹模式下，我们的解密方法不在当前的classNode
            if (processed && !boom) {
                injectDecryptMethod(classNode);
            }
        }
    }

    // Allatori doesn't have this shit
    private static boolean isStringConcatFactory(InvokeDynamicInsnNode invokeDynamic) {
        return "makeConcatWithConstants".equals(
                invokeDynamic.name) && "java/lang/invoke/StringConcatFactory".equals(
                invokeDynamic.bsm.getOwner());
    }

    private static void processStringConstant(ClassNode classNode, MethodNode method, InsnList insns, LdcInsnNode ldc) {
        String original = (String) ldc.cst;
        processString(classNode, method, insns, ldc, original);
    }

    private static boolean processInvokeDynamic(ClassNode classNode, MethodNode method,
                                                InsnList insns, InvokeDynamicInsnNode invokeDynamic
    ) {
        boolean processed = false;
        for (Object arg : invokeDynamic.bsmArgs) {
            if (arg instanceof String) {
                String original = (String) arg;
                // Do not process empty string calls
                if (((String) arg).isEmpty()) {
                    continue;
                }
                processString(classNode, method, insns, invokeDynamic, original);
                processed = true;
            }
        }
        return processed;
    }

    private static void processString(
            ClassNode classNode, MethodNode method, InsnList insns, AbstractInsnNode node, String original
    ) {
        // canonical name
        String key = classNode.name.replace('/', '.') + combiner + method.name;
        // Bruh
        int randomKey = MathUtil.randomInt(min, max);

        int keyForProgram = randomKey & 0xFF;

        // & 255; case 0~254, default bruh
        if (keyStore.containsKey(Integer.valueOf(keyForProgram))) {
            randomKey = keyStore.get(Integer.valueOf(keyForProgram));
        } else {
            keyStore.put(keyForProgram, randomKey);
        }

        String encrypted = xor(original, key, randomKey);

        InsnList newList = new InsnList();
        newList.add(new LdcInsnNode(encrypted));
        newList.add(new LdcInsnNode(keyForProgram));

        if (!boom) {
            newList.add(new MethodInsnNode(
                    INVOKESTATIC,
                    classNode.name,
                    a_methodName,
                    _String_int_returns_String_descriptor,
                    false
            ));
        } else {
            newList.add(new MethodInsnNode(
                    INVOKESTATIC,
                    boomNodeName,
                    a_methodName,
                    _String_int_returns_String_descriptor,
                    false
            ));
        }


        insns.insert(node, newList);
        insns.remove(node);
    }

    private static void removeDecryptMethod(ClassNode classNode) {
        classNode.methods.removeIf(
                method -> method.name.equals(a_methodName) && method.desc.equals(
                        _String_int_returns_String_descriptor));
    }

    /**
     * Vibe coded but kinda useful.
     * <p>
     * 注入一个私有静态解密方法到给定的 ClassNode 中。
     * </p>
     * <p>
     * 解密方法接收一个加密字符串，并返回解密后的字符串。
     * </p>
     * <p>
     * 解密密钥是根据调用者的类名和方法名动态生成的。
     * </p>
     * <p>
     * 此外，它根据输入字符串的长度自动生成不同的 extraXorKey。
     * </p>
     */
    private static void injectDecryptMethod(ClassNode classNode) {
        // 检查解密方法是否已存在，防止重复注入
        for (MethodNode existingMethod : classNode.methods) {
            if (existingMethod.name.equals(a_methodName) && existingMethod.desc.equals(_String_int_returns_String_descriptor)) {
                return; // 方法已存在，无需注入
            }
        }

        // 定义方法访问标志和签名
        // 假设 'methodName' 和 'descriptor' 是预先定义的常量或字段
        MethodBuilder methodBuilder = MethodBuilder.builder()
                .name(a_methodName)
                .access(ACC_PRIVATE | ACC_STATIC)
                .desc("(Ljava/lang/String;I)Ljava/lang/String;")
                .build();


        MethodNode methodNode = methodBuilder.buildNode();
        // 如果 GOTOASMUtils 需要，应用 synthetic 和 bridge 标志
        if (GOTOASMUtils.shouldMarkAsSynthetic(methodNode)) {
            methodNode.access |= ACC_SYNTHETIC;
        }
        if (GOTOASMUtils.shouldMarkAsBridge(methodNode)) {
            methodNode.access |= ACC_BRIDGE;
        }


        // --- 方法代码生成开始 ---
        methodNode.visitCode();

        // 假设 methodNode 是已有的 MethodVisitor
        Label defaultLabel = new Label();
        Label endSwitch = new Label();
        final int cases = 255;
        Label[] labels = new Label[cases];
        for (int i = 0; i < cases; i++) {
            labels[i] = new Label();
        }

        // 加载第二个 int 实参
        methodNode.visitVarInsn(Opcodes.ILOAD, 1);

        // 压入 255
        methodNode.visitIntInsn(Opcodes.SIPUSH, 255);

        // 按位与
        methodNode.visitInsn(Opcodes.IAND);

        // 创建 table switch
        methodNode.visitTableSwitchInsn(0, 254, defaultLabel, labels);

        // 生成每个 case 分支
        for (int i = 0; i < cases; i++) {
            methodNode.visitLabel(labels[i]);
            // 给第二个实参赋一个随机值
            int randomVal = keyStore.getOrDefault(
                    Integer.valueOf(i),
                    // Confuse
                    ThreadLocalRandomManager.theThreadLocalRandom.nextInt(min, max)
            );
            methodNode.visitIntInsn(Opcodes.SIPUSH, randomVal);
            methodNode.visitVarInsn(Opcodes.ISTORE, 1);
            methodNode.visitJumpInsn(Opcodes.GOTO, endSwitch);
        }

        // default 分支
        methodNode.visitLabel(defaultLabel);
        {
            // Default was 255, due to & 255
            int randomVal = keyStore.getOrDefault(
                    Integer.valueOf(255),
                    // Confuse
                    ThreadLocalRandomManager.theThreadLocalRandom.nextInt(min, max)
            );
            methodNode.visitIntInsn(Opcodes.SIPUSH, randomVal);
            methodNode.visitVarInsn(Opcodes.ISTORE, 1);
            methodNode.visitJumpInsn(Opcodes.GOTO, endSwitch);
        }
        methodNode.visitJumpInsn(Opcodes.GOTO, endSwitch);

        // 结束标签
        methodNode.visitLabel(endSwitch);

        // new java/lang/RuntimeException
        methodNode.visitTypeInsn(NEW, "java/lang/RuntimeException");
        // dup
        methodNode.visitInsn(DUP);
        // invokespecial java/lang/RuntimeException.<init>:()V
        methodNode.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false);
        // astore_2 (rte)
        methodNode.visitVarInsn(ASTORE, 2);

        // aload_2
        methodNode.visitVarInsn(ALOAD, 2);
        // invokevirtual java/lang/RuntimeException.getStackTrace:()[Ljava/lang/StackTraceElement;
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/RuntimeException", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
        // iconst_1
        methodNode.visitInsn(ICONST_1);
        // aaload
        methodNode.visitInsn(AALOAD);
        // astore_3 (stackTraceElement)
        methodNode.visitVarInsn(ASTORE, 3);

        // aload_0 (s)
        methodNode.visitVarInsn(ALOAD, 0);
        // invokevirtual java/lang/String.toCharArray:()[C
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
        // astore 4 (in)
        methodNode.visitVarInsn(ASTORE, 4);

        // ldc combiner
        methodNode.visitLdcInsn(combiner);
        // astore 5 (string)
        methodNode.visitVarInsn(ASTORE, 5);

        // new java/lang/StringBuilder
        methodNode.visitTypeInsn(NEW, "java/lang/StringBuilder");
        // dup
        methodNode.visitInsn(DUP);
        // invokespecial java/lang/StringBuilder.<init>:()V
        methodNode.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        // aload_3
        methodNode.visitVarInsn(ALOAD, 3);
        // invokevirtual java/lang/StackTraceElement.getClassName:()Ljava/lang/String;
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
        // invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // aload 5
        methodNode.visitVarInsn(ALOAD, 5);
        // invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // aload_3
        methodNode.visitVarInsn(ALOAD, 3);
        // invokevirtual java/lang/StackTraceElement.getMethodName:()Ljava/lang/String;
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
        // invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // invokevirtual java/lang/StringBuilder.toString:()Ljava/lang/String;
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        // invokevirtual java/lang/String.toCharArray:()[C
        methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
        // astore 6 (k)
        methodNode.visitVarInsn(ASTORE, 6);

        // aload 4
        methodNode.visitVarInsn(ALOAD, 4);
        // arraylength
        methodNode.visitInsn(ARRAYLENGTH);
        // newarray 10 (char)
        methodNode.visitIntInsn(NEWARRAY, T_CHAR);
        // astore 7 (out)
        methodNode.visitVarInsn(ASTORE, 7);

        // iconst_0
        methodNode.visitInsn(ICONST_0);
        // istore 8 (j)
        methodNode.visitVarInsn(ISTORE, 8);

        // For-loop structure
        Label loopCondition = new Label();
        methodNode.visitLabel(loopCondition);
        // iload 8
        methodNode.visitVarInsn(ILOAD, 8);
        // aload 4
        methodNode.visitVarInsn(ALOAD, 4);
        // arraylength
        methodNode.visitInsn(ARRAYLENGTH);
        Label loopEnd = new Label();
        // if_icmpge L3
        methodNode.visitJumpInsn(IF_ICMPGE, loopEnd);

        Label loopBody = new Label();
        methodNode.visitLabel(loopBody);
        // aload 7 (out)
        methodNode.visitVarInsn(ALOAD, 7);
        // iload 8 (j)
        methodNode.visitVarInsn(ILOAD, 8);
        // aload 4 (in)
        methodNode.visitVarInsn(ALOAD, 4);
        // iload 8 (j)
        methodNode.visitVarInsn(ILOAD, 8);
        // caload
        methodNode.visitInsn(CALOAD);
        // aload 6 (k)
        methodNode.visitVarInsn(ALOAD, 6);
        // iload 8 (j)
        methodNode.visitVarInsn(ILOAD, 8);
        // aload 6 (k)
        methodNode.visitVarInsn(ALOAD, 6);
        // arraylength
        methodNode.visitInsn(ARRAYLENGTH);
        // irem
        methodNode.visitInsn(IREM);
        // caload
        methodNode.visitInsn(CALOAD);
        // ixor
        methodNode.visitInsn(IXOR);
        // iload_1 (i)
        methodNode.visitVarInsn(ILOAD, 1);
        // ixor
        methodNode.visitInsn(IXOR);
        // i2c
        methodNode.visitInsn(I2C);
        // castore
        methodNode.visitInsn(CASTORE);

        // iinc 8 1 (j++)
        methodNode.visitIincInsn(8, 1);
        // goto L2
        methodNode.visitJumpInsn(GOTO, loopCondition);

        methodNode.visitLabel(loopEnd);
        // new java/lang/String
        methodNode.visitTypeInsn(NEW, "java/lang/String");
        // dup
        methodNode.visitInsn(DUP);
        // aload 7
        methodNode.visitVarInsn(ALOAD, 7);
        // invokespecial java/lang/String.<init>:([C)V
        methodNode.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
        // Optimization
        methodNode.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/String", "intern", "()Ljava/lang/String;", false);
        // areturn
        methodNode.visitInsn(ARETURN);

        Label endLabel = new Label();
        methodNode.visitLabel(endLabel);

        // 设置操作数栈和局部变量表的最大大小。
        // 使用 ClassWriter.COMPUTE_MAXS 会自动计算，更方便。
        methodNode.visitMaxs(0, 0); // 让 ASM 自动计算最大栈和局部变量数
        methodNode.visitEnd();
        // 将新创建的方法添加到类节点中
        classNode.methods.add(methodNode);
    }

    private static String xor(String input, String key, int extra) {
        char[] in = input.toCharArray();
        char[] k = key.toCharArray();
        // Create the buffer
        char[] out = new char[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (char)
                    (
                            // Original
                            in[i]
                                    // key
                                    ^ k[i % k.length]
                                    // Random constant
//                    ^ extraKeyForCurrentString
//                    ^ extraXorKey
                                    ^ extra
                    );
        }
        return new String(out);
    }
}