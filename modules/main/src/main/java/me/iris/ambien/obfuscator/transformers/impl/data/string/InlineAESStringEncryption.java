package me.iris.ambien.obfuscator.transformers.impl.data.string;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.utilities.kek.myj2c.Myj2cASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/*
 * a114-klassmaster
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 *
 * Fixed by Gemini. Optimized variable usage and fixed VerifyError.
 */
@SuppressWarnings("FieldMayBeFinal")
public class InlineAESStringEncryption implements Opcodes {

    private static String algorithm = "AES";
    private static String algorithmFullName = algorithm + "/CBC/PKCS5Padding";

    public static void doit(ClassNode node) {
        // Interface methods (static) in Java 8+ can have bodies, but usually safe to skip for complex logic if needed.
        // If you want to support them, keep logic, but ensure maxLocals is handled correctly.
        if (Myj2cASMUtils.isClassNodeInterface(node)) return;

        try {
            if (node.methods == null || node.methods.isEmpty()) return;

            for (MethodNode method : node.methods) {
                if (!Ambien.exclusionManager.isMethodExcluded(node.name, method.name)) {
                    // CRITICAL FIX: Remove existing frames to force ClassWriter to recompute them.
                    // This prevents "Bad local variable type" caused by stale stack map frames.
                    removeExistingFrames(method);
                    encryptMethodStrings(method, node);
                }
            }

        } catch (Exception ex) {
            Ambien.logger.error("Error applying inline AES string encryption", ex);
        }
    }

    // Helper to remove frames
    private static void removeExistingFrames(MethodNode mn) {
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        for (AbstractInsnNode insn : instructions) {
            if (insn instanceof FrameNode) {
                mn.instructions.remove(insn);
            }
        }
    }

    private static void encryptMethodStrings(MethodNode mn, ClassNode node) {
        if (mn == null || node == null) return;

        List<LdcInsnNode> ldcNodes = new ArrayList<>();
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof LdcInsnNode) {
                LdcInsnNode ldc = (LdcInsnNode) insn;
                if (ldc.cst instanceof String) {
                    String s = (String) ldc.cst;
                    // Encrypt only strings longer than 1 char to keep code size reasonable
                    if (s.length() > 1) {
                        ldcNodes.add(ldc);
                    }
                }
            }
        }

        if (ldcNodes.isEmpty()) return;

        InsnList decryptBlock = new InsnList();
        SecureRandom sr = new SecureRandom();

        // --- OPTIMIZATION: Allocate temporary local variables ONCE per method ---
        // Instead of creating new slots for Cipher/Key/Spec for every string, reuse them.
        int varKeyBytes = mn.maxLocals++;
        int varIvBytes = mn.maxLocals++;
        int varEncryptedBytes = mn.maxLocals++; // Holds the byte[] to decrypt
        int varKeySpec = mn.maxLocals++;
        int varIvSpec = mn.maxLocals++;
        int varCipher = mn.maxLocals++;

        // 1. Initialize the Cipher instance ONCE at the start of the block
        // Cipher c = Cipher.getInstance(...)
        decryptBlock.add(new LdcInsnNode(algorithmFullName));
        decryptBlock.add(new MethodInsnNode(INVOKESTATIC, "javax/crypto/Cipher", "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;", false));
        decryptBlock.add(new VarInsnNode(ASTORE, varCipher));

        Map<LdcInsnNode, Integer> ldcToVarMap = new HashMap<>();

        for (LdcInsnNode ldc : ldcNodes) {
            String originalString = (String) ldc.cst;

            // Generate unique Key/IV for this string
            byte[] keyBytes = new byte[16];
            byte[] iv = new byte[16];
            sr.nextBytes(keyBytes);
            sr.nextBytes(iv);

            byte[] encryptedBytes = encryptAES(originalString, keyBytes, iv);

            // Allocate a unique variable for the FINAL decrypted string result
            int decryptedStringVar = mn.maxLocals++;
            ldcToVarMap.put(ldc, decryptedStringVar);

            // Generate decryption instructions reusing the temp variables
            InsnList decryptInsn = new InsnList();

            // Store Key bytes
            decryptInsn.add(Myj2cASMUtils.createByteArrayFromString(keyBytes));
            decryptInsn.add(new VarInsnNode(ASTORE, varKeyBytes));

            // Store IV bytes
            decryptInsn.add(Myj2cASMUtils.createByteArrayFromString(iv));
            decryptInsn.add(new VarInsnNode(ASTORE, varIvBytes));

            // Store Encrypted content bytes
            decryptInsn.add(Myj2cASMUtils.createByteArrayFromString(encryptedBytes));
            decryptInsn.add(new VarInsnNode(ASTORE, varEncryptedBytes));

            // Create SecretKeySpec
            decryptInsn.add(new TypeInsnNode(NEW, "javax/crypto/spec/SecretKeySpec"));
            decryptInsn.add(new InsnNode(DUP));
            decryptInsn.add(new VarInsnNode(ALOAD, varKeyBytes));
            decryptInsn.add(new LdcInsnNode(algorithm));
            decryptInsn.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/SecretKeySpec", "<init>", "([BLjava/lang/String;)V", false));
            decryptInsn.add(new VarInsnNode(ASTORE, varKeySpec));

            // Create IvParameterSpec
            decryptInsn.add(new TypeInsnNode(NEW, "javax/crypto/spec/IvParameterSpec"));
            decryptInsn.add(new InsnNode(DUP));
            decryptInsn.add(new VarInsnNode(ALOAD, varIvBytes));
            decryptInsn.add(new MethodInsnNode(INVOKESPECIAL, "javax/crypto/spec/IvParameterSpec", "<init>", "([B)V", false));
            decryptInsn.add(new VarInsnNode(ASTORE, varIvSpec));

            // Init Cipher (DECRYPT_MODE)
            decryptInsn.add(new VarInsnNode(ALOAD, varCipher));
            decryptInsn.add(new LdcInsnNode(Cipher.DECRYPT_MODE));
            decryptInsn.add(new VarInsnNode(ALOAD, varKeySpec));
            decryptInsn.add(new VarInsnNode(ALOAD, varIvSpec));
            decryptInsn.add(new MethodInsnNode(INVOKEVIRTUAL, "javax/crypto/Cipher", "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V", false));

            // DoFinal
            decryptInsn.add(new VarInsnNode(ALOAD, varCipher));
            decryptInsn.add(new VarInsnNode(ALOAD, varEncryptedBytes));
            decryptInsn.add(new MethodInsnNode(INVOKEVIRTUAL, "javax/crypto/Cipher", "doFinal", "([B)[B", false));

            // New String(bytes, UTF8)
            decryptInsn.add(new TypeInsnNode(NEW, "java/lang/String"));
            decryptInsn.add(new InsnNode(DUP_X1)); // Swap: StringRef, Bytes -> Bytes, StringRef
            decryptInsn.add(new InsnNode(SWAP));   // -> StringRef, Bytes
            decryptInsn.add(new FieldInsnNode(GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;"));
            decryptInsn.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V", false));

            // Intern
            decryptInsn.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "intern", "()Ljava/lang/String;", false));

            // Store result
            decryptInsn.add(new VarInsnNode(ASTORE, decryptedStringVar));

            decryptBlock.add(decryptInsn);
        }

        mn.instructions.insert(decryptBlock);

        // Replace original LDCs with ALOADs
        for (Map.Entry<LdcInsnNode, Integer> entry : ldcToVarMap.entrySet()) {
            LdcInsnNode ldc = entry.getKey();
            int varIndex = entry.getValue();
            mn.instructions.insertBefore(ldc, new VarInsnNode(ALOAD, varIndex));
            mn.instructions.remove(ldc);
        }
    }

    private static byte[] encryptAES(String input, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(algorithmFullName);
            SecretKeySpec skeySpec = new SecretKeySpec(key, algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));
            return cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}