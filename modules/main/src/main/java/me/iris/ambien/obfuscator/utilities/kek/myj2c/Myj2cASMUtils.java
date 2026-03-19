package me.iris.ambien.obfuscator.utilities.kek.myj2c;

import cn.a114.commonutil.fixing.AntiNPE;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public final class Myj2cASMUtils implements Opcodes {

    // Do not modify interfaces
    public static boolean classNodeIsNotInterface(@NotNull ClassNode classNode) {
        AntiNPE.checkNonNull(classNode);
        // Check if the class is not an interface
        return (classNode.access & ACC_INTERFACE) == 0;
    }

    public static boolean isClassNodeInterface(@NotNull ClassNode classNode) {
        // Check if the class is an interface
        return (classNode.access & ACC_INTERFACE) != 0;
    }

    public static boolean isMethodNative(@NotNull MethodNode methodNode) {
        AntiNPE.checkNonNull(methodNode);
        // Check if the method is native
        return (methodNode.access & ACC_NATIVE) != 0;
    }

    public static boolean isClassAbstract(@NotNull ClassNode classNode) {
        AntiNPE.checkNonNull(classNode);
        // Check if the class is abstract
        return (classNode.access & ACC_ABSTRACT) != 0;
    }

    /**
     * Checks if a class is eligible to be modified.
     * A class is eligible to be modified if it is not an interface, not abstract, and does not contain native methods.
     * @param classNode the class node to check
     *                  @return true if the class is eligible
     *                  false otherwise
     */
    public static boolean isMethodEligibleToModify(@NotNull ClassNode classNode, @NotNull MethodNode methodNode) {
        try {
            AntiNPE.checkNonNull(classNode);
            AntiNPE.checkNonNull(methodNode);
        } catch (NullPointerException e) {
            return false;
        }

        return classNodeIsNotInterface(classNode) && !isClassAbstract(classNode) && !isMethodNative(methodNode);
    }

    /**
     * org.objectweb.asm.Type#getInternalName()
     * <br>
     * java.lang.Class#getCanonicalName
     * @see <a href="https://github.com/Col-E/Useful-Things/blob/master/reversing/re-01-java-compiling.md#why-is-there-a-javaioprintstream-and-ljavaioprintstream">This</a>
     */
    public static String getName(@NotNull ClassNode classNode) {
        AntiNPE.checkNonNull(classNode);
        return classNode.name.replace("/", ".");
    }

    public static String getName(@NotNull ClassNode classNode, @NotNull FieldNode fieldNode) {
        AntiNPE.checkNonNull(classNode);
        return classNode.name + "." + fieldNode.name;
    }

    public static String getName(@NotNull ClassNode classNode, @NotNull MethodNode methodNode) {
        return classNode.name + "." + methodNode.name + methodNode.desc;
    }

    public static MethodNode findOrCreateInit(ClassNode classNode) {
        MethodNode clinit = findMethod(classNode, "<init>", "()V");
        if (clinit == null) {
            clinit = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
            clinit.instructions.add(new InsnNode(RETURN));
            classNode.methods.add(clinit);
        }
        return clinit;
    }

    public static MethodNode findOrCreateClinit(ClassNode classNode) {
        MethodNode clinit = findMethod(classNode, "<clinit>", "()V");
        if (clinit == null) {
            clinit = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
            clinit.instructions.add(new InsnNode(RETURN));
            classNode.methods.add(clinit);
        }
        return clinit;
    }

    public static MethodNode findMethod(ClassNode classNode, String name, String desc) {
        return classNode.methods
                .stream()
                .filter(methodNode -> name.equals(methodNode.name) && desc.equals(methodNode.desc))
                .findAny()
                .orElse(null);
    }

    public static boolean isInvokeMethod(AbstractInsnNode insn, boolean includeInvokeDynamic) {
        return insn.getOpcode() >= INVOKEVIRTUAL && (includeInvokeDynamic ? insn.getOpcode() <= INVOKEDYNAMIC : insn.getOpcode() < INVOKEDYNAMIC);
    }

    public static boolean isFieldInsn(AbstractInsnNode insn) {
        return insn.getOpcode() >= GETSTATIC && insn.getOpcode() <= PUTFIELD;
    }


    /**
     *  Pushes a long value onto the stack.
     */
    public static AbstractInsnNode pushLong(long value) {

        if (value == 0) {
            return new InsnNode(LCONST_0);
        }

        if (value == 1) {
            return new InsnNode(LCONST_1);
        }
        return new LdcInsnNode(value);
    }

    public static AbstractInsnNode pushInt(int value) {
        if (value >= -1 && value <= 5) {
            return new InsnNode(ICONST_0 + value);
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IntInsnNode(BIPUSH, value);
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(SIPUSH, value);
        }
        return new LdcInsnNode(value);
    }

    /**
     * 将字节数组转换为 ASM 指令序列
     * => byte[] arr = new byte[size]; arr[0] = val; ... return arr;
     */
    public static InsnList createByteArrayInsn(byte[] data) {
        InsnList insns = new InsnList();
        insns.add(Myj2cASMUtils.pushInt(data.length));
        insns.add(new IntInsnNode(NEWARRAY, T_BYTE));

        for (int i = 0; i < data.length; i++) {
            insns.add(new InsnNode(DUP));
            insns.add(new IntInsnNode(BIPUSH, i));
            insns.add(new IntInsnNode(BIPUSH, data[i]));
            insns.add(new InsnNode(BASTORE));
        }
        return insns;
    }


    /**
     * 将字节数组转换为 ASM 指令序列
     * => byte[] arr = new byte[size]; arr[0] = val; ... return arr;
     */
    public static InsnList createByteArrayFromString(byte[] data) {
        InsnList insns = new InsnList();

        // 1. 将 byte[] 转换为 ISO-8859-1 字符串
        // 这样每个 byte 都会完美映射为一个 char，不会丢失数据
        String strData = new String(data, StandardCharsets.ISO_8859_1);

        // 2. LDC "字符串"
        insns.add(new LdcInsnNode(strData));

        // 3. 获取 StandardCharsets.ISO_8859_1 静态字段
        // 签名: Ljava/nio/charset/Charset;
        insns.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                "java/nio/charset/StandardCharsets",
                "ISO_8859_1",
                "Ljava/nio/charset/Charset;"
        ));

        // 4. 调用 String.getBytes(Charset)
        // 签名: (Ljava/nio/charset/Charset;)[B
        insns.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "getBytes",
                "(Ljava/nio/charset/Charset;)[B",
                false
        ));

        return insns;
    }


    /**
     * Adds static String[] fields `a` and `b` to the class if they do not already exist.
     */
    public static void addStringArrayFields(@NotNull ClassNode classNode) {
        if (classNode == null) return;

        if (!hasField(classNode, "a", "[Ljava/lang/String;")) {
            FieldNode fieldA = new FieldNode(
                    ACC_PRIVATE | ACC_STATIC,
                    "a",
                    "[Ljava/lang/String;",
                    null,
                    null
            );
            classNode.fields.add(fieldA);
        }

        if (!hasField(classNode, "b", "[Ljava/lang/String;")) {
            FieldNode fieldB = new FieldNode(
                    ACC_PRIVATE | ACC_STATIC,
                    "b",
                    "[Ljava/lang/String;",
                    null,
                    null
            );
            classNode.fields.add(fieldB);
        }
    }

    /**
     * Checks if the class already has a field with the same name and descriptor.
     */
    public static boolean hasField(@NotNull ClassNode classNode, @NotNull String name, @NotNull String desc) {
        for (FieldNode field : classNode.fields) {
            if (field.name.equals(name) && field.desc.equals(desc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects duplicate field names in the class (ignoring descriptor).
     */
    public static Set<String> detectDuplicateFieldNames(@NotNull ClassNode classNode) {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for (FieldNode field : classNode.fields) {
            if (!seen.add(field.name)) {
                duplicates.add(field.name);
            }
        }
        return duplicates;
    }
}
