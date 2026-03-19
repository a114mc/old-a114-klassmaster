/*
 * Bruhfuscator-CN
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.utilities;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.transformers.impl.exploits.SyntheticMarker;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class GOTOASMUtils {

    /**
     * LDC string checker
     *
     * @param node The node to check
     * @return The node is ldc String
     */
    @Contract(value = "null -> false", pure = true)
    public static boolean isInsnNodeString(AbstractInsnNode node) {
        if (node == null) {
            return false;
        }
        return node instanceof LdcInsnNode
                && ((LdcInsnNode) node).cst != null
                && ((LdcInsnNode) node).cst instanceof String;
    }

    public static String getString(AbstractInsnNode node) {
        return ((LdcInsnNode) node).cst.toString();
    }

    public static Number getNumber(AbstractInsnNode node) {
        if (node instanceof LdcInsnNode) {
            return (Number) ((LdcInsnNode) node).cst;
        }
        int opcode = node.getOpcode();
        if (node instanceof IntInsnNode) {
            if (opcode == Opcodes.SIPUSH) {
                return (short) ((IntInsnNode) node).operand;
            }
            if (opcode == Opcodes.BIPUSH) {
                return (byte) ((IntInsnNode) node).operand;
            }
        } else {
            switch (opcode) {
                case Opcodes.ICONST_M1:
                    return -1;
                case Opcodes.ICONST_0:
                    return 0;
                case Opcodes.ICONST_1:
                    return 1;
                case Opcodes.ICONST_2:
                    return 2;
                case Opcodes.ICONST_3:
                    return 3;
                case Opcodes.ICONST_4:
                    return 4;
                case Opcodes.ICONST_5:
                    return 5;
                case Opcodes.LCONST_0:
                    return 0L;
                case Opcodes.LCONST_1:
                    return 1L;
                case Opcodes.FCONST_0:
                    return 0.0F;
                case Opcodes.FCONST_1:
                    return 1.0F;
                case Opcodes.FCONST_2:
                    return 2.0F;
                case Opcodes.DCONST_0:
                    return 0.0;
                case Opcodes.DCONST_1:
                    return 1.0;
                default:
                    throw new IllegalStateException("Unexpected value: " + opcode);
            }
        }
        // Never reached, but required by the compiler
        throw new IllegalArgumentException();
    }

    public static boolean isNumber(@NotNull AbstractInsnNode node) {
        int opcode = node.getOpcode();
        if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.SIPUSH) {
            return true;
        }

        return node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Number;
    }

    @Contract("_ -> new")
    public static @NotNull AbstractInsnNode createNumberNode(int value) {
        int opcode = getNumberOpcode(value);
        switch (opcode) {
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
                return new InsnNode(opcode);
            default:
                if (value >= -128 && value <= 127) {
                    return new IntInsnNode(Opcodes.BIPUSH, value);
                }
                if (value >= -32768 && value <= 32767) {
                    return new IntInsnNode(Opcodes.SIPUSH, value);
                }
                return new LdcInsnNode(value);
        }
    }

    @Contract("_ -> new")
    public static @NotNull AbstractInsnNode createNumberNode(short value) {
        switch (value) {
            case -1:
                return new InsnNode(Opcodes.ICONST_M1);
            case 0:
                return new InsnNode(Opcodes.ICONST_0);
            case 1:
                return new InsnNode(Opcodes.ICONST_1);
            case 2:
                return new InsnNode(Opcodes.ICONST_2);
            case 3:
                return new InsnNode(Opcodes.ICONST_3);
            case 4:
                return new InsnNode(Opcodes.ICONST_4);
            case 5:
                return new InsnNode(Opcodes.ICONST_5);
            default:
                if (value >= -128 && value <= 127) {
                    return new IntInsnNode(Opcodes.BIPUSH, value);
                }
                return new IntInsnNode(Opcodes.SIPUSH, value);
        }
    }

    @Contract("_ -> new")
    public static @NotNull AbstractInsnNode createNumberNode(byte value) {
        switch (value) {
            case -1:
                return new InsnNode(Opcodes.ICONST_M1);
            case 0:
                return new InsnNode(Opcodes.ICONST_0);
            case 1:
                return new InsnNode(Opcodes.ICONST_1);
            case 2:
                return new InsnNode(Opcodes.ICONST_2);
            case 3:
                return new InsnNode(Opcodes.ICONST_3);
            case 4:
                return new InsnNode(Opcodes.ICONST_4);
            case 5:
                return new InsnNode(Opcodes.ICONST_5);
            default:
                return new IntInsnNode(Opcodes.BIPUSH, value);
        }
    }

    @Contract(pure = true)
    public static int getNumberOpcode(int value) {
        switch (value) {
            case -1:
                return Opcodes.ICONST_M1;
            case 0:
                return Opcodes.ICONST_0;
            case 1:
                return Opcodes.ICONST_1;
            case 2:
                return Opcodes.ICONST_2;
            case 3:
                return Opcodes.ICONST_3;
            case 4:
                return Opcodes.ICONST_4;
            case 5:
                return Opcodes.ICONST_5;
            default:
                // The value is outside the range of ICONST_*
                if (value >= -128 && value <= 127) {
                    return Opcodes.BIPUSH;
                }
                return (value >= -32768 && value <= 32767) ? Opcodes.SIPUSH : Opcodes.LDC;
        }
    }

    @Contract(pure = true)
    public static MethodNode getClinitMethodNode(ClassNode node) {
        return getMethodNode(node, "<clinit>");
    }

    /**
     * Returns the init method node of the given class node. If the init method does not exist, it
     * creates a new one with a default implementation.
     *
     * @param node The class node to get the init method from.
     * @return The init method node.
     */
    public static @NotNull MethodNode getInitMethodNode(ClassNode node) {
        MethodNode methodNode = getMethodNode(node, "<init>");

        if (methodNode == null) {
            System.err.println(
                    "WTF?! " + node.name + " doesn't have an init method??!");
            methodNode = new MethodNode(
                    Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            methodNode.visitCode();
            methodNode.visitVarInsn(Opcodes.ALOAD, 0);
            methodNode.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V",
                    false
            );
            methodNode.visitInsn(Opcodes.RETURN);
            methodNode.visitEnd();
        }

        return methodNode;
    }

    @Contract(pure = true)
    public static @Nullable MethodNode getMethodNode(@NotNull ClassNode node, String methodName) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    public static @NotNull MethodNode getClinitMethodNodeOrCreateNew(ClassNode node) {
        MethodNode method = getMethodNode(node, "<clinit>");

        if (method == null) {
            method = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            node.methods.add(method);
        }

        return method;
    }

    public static void computeMaxLocals(@NotNull MethodNode method) {
        int maxLocals = Type.getArgumentsAndReturnSizes(method.desc) >> 2;

        for (AbstractInsnNode node : method.instructions) {
            if (node instanceof VarInsnNode) {
                int local = ((VarInsnNode) node).var;
                int size =
                        (node.getOpcode() == Opcodes.LLOAD
                                        || node.getOpcode() == Opcodes.DLOAD
                                        || node.getOpcode() == Opcodes.LSTORE
                                        || node.getOpcode() == Opcodes.DSTORE)
                                ? 2
                                : 1;
                maxLocals = Math.max(maxLocals, local + size);
            } else if (node instanceof IincInsnNode) {
                int local = ((IincInsnNode) node).var;
                maxLocals = Math.max(maxLocals, local + 1);
            }
        }

        method.maxLocals = maxLocals;
    }

    @Contract(pure = true)
    public static boolean isInterfaceClass(@NotNull ClassNode node) {
        return (node.access & ACC_INTERFACE) != 0;
    }

    /**
     * Returns if the method is special <br>
     *
     * @param node The method node
     * @return If the method node's access is native or abstract, returns true
     */
    @Contract(pure = true)
    public static boolean isSpecialMethod(@NotNull MethodNode node) {
        return (node.access & ACC_NATIVE) != 0
                ||
                (node.access & ACC_ABSTRACT) != 0;
    }

    public static void addAccess(@NotNull final FieldNode fieldNode, int access) {
        fieldNode.access |= access;
    }

    public static void addAccess(@NotNull final MethodNode mn, int access) {
        mn.access |= access;
    }

    public static void addAccess(@NotNull final ClassNode cn, int access) {
        cn.access |= access;
    }

    /**
     * A simple method to check if the MethodNode is a class initializer or constructor.
     *
     * @param method to check
     * @return is method named as <clinit> or <init>
     */
    public static boolean isMethodNodeInitializerOrConstructor(MethodNode method) {
        if(method == null)return false;
        return (method.name.equals("<clinit>") && method.desc.equals("()V"))
                || method.name.equals("<init>");
    }

    /**
     * A simple method to check if the MethodNode was the program entry method.
     *
     * @param method to check
     * @return is method named as main and signature equals "[Ljava/lang/String;"
     */
    public static boolean isMainMethod(@NotNull MethodNode method) {
        // public static void main(String[] args){}

        return method.name.equals("main")
                && method.desc.equals("([Ljava/lang/String;)V")
                // Why I forgot to check is it static? f that.
                && (method.access & Opcodes.ACC_STATIC) != 0;
    }

    /**
     * A method to prevent synthetic marked for special method
     *
     * @return should you mark the method as synthetic or bridge.
     */
    public static boolean shouldMarkAsSynthetic(MethodNode method) {
        return isSyntheticMarkerEnabled()
                && isMethodSyntheticEnabled()
                && !isMethodNodeInitializerOrConstructor(method)
                && !isMainMethod(method)
        //		       && !isSpecialMethod(method)
        ;
    }
    /**
     * A method to prevent synthetic marked for special field
     *
     * @return should you mark the field as synthetic or bridge.
     */
    public static boolean shouldMarkAsSynthetic(FieldNode method) {
        return isSyntheticMarkerEnabled()
                && isFieldSyntheticEnabled();
    }

    /// A method to prevent synthetic marked for special method
    ///
    /// @return should you mark the method as synthetic or bridge.
    public static boolean shouldMarkAsBridge(MethodNode method) {
        return isSyntheticMarkerEnabled()
                && isMethodBridgeEnabled()
                && !isMethodNodeInitializerOrConstructor(method)
                && !isMainMethod(method);
    }

    public static boolean isSyntheticMarkerEnabled() {
        SyntheticMarker m = null;
        try {
            m =
                    (SyntheticMarker)
                            (Ambien.get
                                    .transformerManager
                                    .getTransformers()
                                    .stream()
                                    .filter(it -> it instanceof SyntheticMarker)
                                    .findFirst()
                                    .get()
                            );
        } catch (Exception e) {
            throw new RuntimeException("WTFK no synthetic marker found?");
        }
        return m.isEnabled();
    }

    public static boolean isMethodSyntheticEnabled() {
        return SyntheticMarker.METHOD_SYNTHETIC.isEnabled();
    }

    public static boolean isMethodBridgeEnabled() {
        return SyntheticMarker.METHOD_BRIDGE.isEnabled();
    }
    public static boolean isFieldSyntheticEnabled(){
        return SyntheticMarker.FIELD_SYNTHETIC.isEnabled();
    }


    public static boolean containsMethod(ClassNode cl, MethodNode mn){
        return getMethodNode(cl,mn.name) != null;
    }
}
