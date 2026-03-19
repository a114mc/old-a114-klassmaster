/*
 * a114-klassmaster
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */
package me.iris.ambien.obfuscator.transformers.impl.data.string;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import me.iris.ambien.obfuscator.utilities.kek.myj2c.Myj2cASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * String encryption, inspired from ZKM9
 */
public class InlineXorStringEncryption implements Opcodes {

    private static final int KEY_LENGTH = 7;

    public static void doit(ClassNode node) {
        if (Myj2cASMUtils.isClassNodeInterface(node)) return;

        try {
            if (node.methods == null || node.methods.isEmpty()) return;

            for(MethodNode methodNode : node.methods){
                if(methodNode.name.contains("<") || GOTOASMUtils.isSpecialMethod(methodNode)) {
                    continue;
                }
                if (why(methodNode, node)) continue;
            }

        } catch (Exception ex) {
            Ambien.logger.error("Error applying ZKM-like string encryption", ex);
        }
    }

    private static boolean why(MethodNode mn, ClassNode node) {
        if(mn == null || node == null) return true;
        List<LdcInsnNode> ldcNodes = new ArrayList<>();
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof LdcInsnNode) {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst instanceof String) {
                    if (((String) ldc.cst).length() > 1) {
                        ldcNodes.add(ldc);
                    }
                }
            }
        }

        if (ldcNodes.isEmpty()) return true;

        Ambien.logger.debug("Applying ZKM-like string encryption to " + node.name);

        // Insert decrypt instructions and replace all plain-text string to encrypted string
        for (LdcInsnNode ldc : ldcNodes) {
            String originalString = (String) ldc.cst;
            byte[] keys = generateKey(KEY_LENGTH);
            byte baseXor = (byte) Math.abs((byte) ThreadLocalRandom.current().nextInt(1, 256));
            char[] encryptedChars = encrypt(originalString.toCharArray(), baseXor, keys);

            InsnList decryptionInstructions = genDecryptInsn(new String(encryptedChars), baseXor, keys, mn);

            mn.instructions.insertBefore(ldc, decryptionInstructions);
            mn.instructions.remove(ldc);
        }
        return false;
    }

    /**
     * Generates decryption bytecode.
     * This final version correctly handles byte sign-extension during XOR operations.
     */
    private static InsnList genDecryptInsn(String encryptedString, byte baseXor, byte[] keys, MethodNode mn) {
        InsnList insns = new InsnList();

        int varArr = mn.maxLocals++;
        int varLength = mn.maxLocals++;
        int varIndex = mn.maxLocals++;

        insns.add(new LdcInsnNode(encryptedString));
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false));
        insns.add(new VarInsnNode(ASTORE, varArr));

        insns.add(new VarInsnNode(ALOAD, varArr));
        insns.add(new InsnNode(ARRAYLENGTH));
        insns.add(new VarInsnNode(ISTORE, varLength));

        insns.add(new InsnNode(ICONST_0));
        insns.add(new VarInsnNode(ISTORE, varIndex));

        LabelNode loopStart = new LabelNode();
        LabelNode loopCheck = new LabelNode();
        insns.add(new JumpInsnNode(GOTO, loopCheck));
        insns.add(loopStart);

        insns.add(new VarInsnNode(ALOAD, varArr));
        insns.add(new VarInsnNode(ILOAD, varIndex));

        insns.add(new VarInsnNode(ALOAD, varArr));
        insns.add(new VarInsnNode(ILOAD, varIndex));
        insns.add(new InsnNode(CALOAD));

        // CRITICAL FIX: Push the byte value directly. It will be sign-extended to an int on the stack,
        // matching Java's behavior in the encrypt method.
        insns.add(Myj2cASMUtils.pushInt(baseXor));
        insns.add(new InsnNode(IXOR));

        LabelNode[] keyLabels = new LabelNode[KEY_LENGTH];
        for (int i = 0; i < KEY_LENGTH; i++) keyLabels[i] = new LabelNode();
        LabelNode switchDefault = new LabelNode();
        LabelNode switchEnd = new LabelNode();

        insns.add(new VarInsnNode(ILOAD, varIndex));
        insns.add(Myj2cASMUtils.pushInt(KEY_LENGTH));
        insns.add(new InsnNode(IREM));
        insns.add(new LookupSwitchInsnNode(switchDefault, createKeys(KEY_LENGTH), keyLabels));

        for (int i = 0; i < KEY_LENGTH; i++) {
            insns.add(keyLabels[i]);
            // CRITICAL FIX: Same here for the key bytes.
            insns.add(Myj2cASMUtils.pushInt(keys[i]));
            insns.add(new JumpInsnNode(GOTO, switchEnd));
        }

        insns.add(switchDefault);
        insns.add(new InsnNode(ICONST_0));
        insns.add(switchEnd);

        insns.add(new InsnNode(IXOR));
        insns.add(new InsnNode(I2C));
        insns.add(new InsnNode(CASTORE));

        insns.add(new IincInsnNode(varIndex, 1));

        insns.add(loopCheck);
        insns.add(new VarInsnNode(ILOAD, varIndex));
        insns.add(new VarInsnNode(ILOAD, varLength));
        insns.add(new JumpInsnNode(IF_ICMPLT, loopStart));

        insns.add(new TypeInsnNode(NEW, "java/lang/String"));
        insns.add(new InsnNode(DUP));
        insns.add(new VarInsnNode(ALOAD, varArr));
        insns.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false));
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "intern", "()Ljava/lang/String;", false));

        return insns;
    }


    private static byte[] generateKey(int length) {
        byte[] temp = new byte[length];
        for (int i = 0; i < length; i++) {
            temp[i] = (byte) Math.abs((byte) Math.abs(ThreadLocalRandom.current().nextInt(1, 256)));
        }
        return temp;
    }

    private static int[] createKeys(int keyLength) {
        int[] arr = new int[keyLength];
        for (int i = 0; i < keyLength; i++) {
            arr[i] = i;
        }
        return arr;
    }

    private static char[] encrypt(char[] chars, byte baseXor, byte[] keys) {
        char[] temp = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            temp[i] = (char) (chars[i] ^ baseXor ^ keys[i % KEY_LENGTH]);
        }
        return temp;
    }
}