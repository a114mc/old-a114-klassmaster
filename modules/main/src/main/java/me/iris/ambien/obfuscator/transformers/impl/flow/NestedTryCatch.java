/*
 * Bruhfuscator-CN
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.transformers.impl.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class NestedTryCatch {

    private static final String name = "fuck";
    private static final String commonExceptionType = Type.getInternalName(Throwable.class);

    /**
     * Generates or retrieves a single fake handler method for the specified exception type.
     * This method ensures only one helper method is created for all try-catch blocks.
     *
     * @param cn The class node.
     * @return The corresponding method node for the handler.
     */
    private static MethodNode getOrCreateFakeExceptionHandler(ClassNode cn) {
        String descriptor = String.format("(L%s;)L%s;", commonExceptionType, commonExceptionType);

        // Check if the method already exists
        for (MethodNode method : cn.methods) {
            if (method.name.equals(name) && method.desc.equals(descriptor)) {
                return method;
            }
        }

        // Create a new public static Throwable a(Throwable t) method
        MethodNode decoyMethod = new MethodNode(
                ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                name,
                descriptor,
                null,
                null);

        // Method body: return t;
        decoyMethod.instructions.add(new VarInsnNode(ALOAD, 0));
        decoyMethod.instructions.add(new InsnNode(ARETURN));
        cn.methods.add(decoyMethod);
        return decoyMethod;
    }

    /**
     * Adds multi-layered nested try-catch obfuscation to a class's methods.
     *
     * @param node The class node to obfuscate.
     * @param depth The nesting depth.
     */
    public static void add(ClassNode node, int depth) {
        if (node == null || node.methods == null || depth <= 0) {
            return;
        }

        // Create a copy of the methods list to avoid ConcurrentModificationException
        List<MethodNode> methodsToProcess = new ArrayList<>(node.methods);

        // Iterate through the copy of the methods list
        for (MethodNode method : methodsToProcess) {
            // Skip abstract, static, or native methods
            if (GOTOASMUtils.isSpecialMethod(method)) {
                continue;
            }

            if (Ambien.exclusionManager.isMethodExcluded(node.name, method)) {
                continue;
            }

            // Skip constructors and the helper method itself
            if (method.name.equals("<init>") || method.name.equals(name)) {
                continue;
            }

            // Get the method's instruction list
            InsnList instructions = method.instructions;
            if (instructions.size() < 2) {
                continue;
            }

            // Store original instructions and clear the method's list for reconstruction
            InsnList originalInstructions = method.instructions;
            method.instructions = new InsnList();

            // Recursively insert nested try-catch blocks
            insertNestedTryCatch(node, method, originalInstructions, depth);
            Ambien.logger.debug("Inserted nested try-catch into class " + node.name + ", method " + method.name);
        }
    }

    /**
     * Recursive method to generate a multi-layered try-catch structure.
     *
     * @param cn The class node.
     * @param method The method node to modify.
     * @param instructions The instruction list to wrap.
     * @param depth The current recursion depth.
     */
    private static void insertNestedTryCatch(ClassNode cn, MethodNode method, InsnList instructions, int depth) {
        if(method.tryCatchBlocks == null || method.tryCatchBlocks.isEmpty()){
            // ?
            method.instructions = instructions;
            return;
        }
        if (depth <= 0) {
            // Base case: add the original instructions to the innermost try block
            method.instructions.add(instructions);
            return;
        }

        // Create labels for the try-catch block
        LabelNode startLabel = new LabelNode();
        LabelNode endLabel = new LabelNode();
        LabelNode handlerLabel = new LabelNode();

        // Add start label for the try block
        method.instructions.add(startLabel);

        // Recursive call to insert the next layer of try-catch
        insertNestedTryCatch(cn, method, instructions, depth - 1);

        // Add the end label for the try block
        method.instructions.add(endLabel);

        // Add the exception table entry
        method.tryCatchBlocks.add(new TryCatchBlockNode(
                startLabel,
                endLabel,
                handlerLabel,
                commonExceptionType));

        // Add the catch block's handler label and instructions
        method.instructions.add(handlerLabel);

        // 保存异常到新的局部变量
        int localIndex = method.maxLocals++;
        method.instructions.add(new VarInsnNode(ASTORE, localIndex));

        // 调用假异常处理方法
        MethodNode decoy = getOrCreateFakeExceptionHandler(cn);
        method.instructions.add(new VarInsnNode(ALOAD, localIndex));
        method.instructions.add(new MethodInsnNode(
                INVOKESTATIC,
                cn.name,
                decoy.name,
                decoy.desc,
                false));

        // Re-throw the exception
        method.instructions.add(new InsnNode(ATHROW));

        // Increment maxLocals to account for the new local variable
        method.maxLocals++;
    }
}