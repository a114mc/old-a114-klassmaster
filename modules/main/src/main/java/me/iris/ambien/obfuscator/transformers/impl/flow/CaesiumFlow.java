/*
 * Bruhfuscator-CN
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.transformers.impl.flow;

import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import me.iris.ambien.obfuscator.wrappers.MethodWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.concurrent.ThreadLocalRandom;
public class CaesiumFlow implements Opcodes {
    private static String jumpIntCondField;
    private static String jumpBoolCondField;

    private static int jumpIntCond;
    private static boolean jumpBoolCond;

    public static void transform(JarWrapper shit) {
        shit.getClasses().forEach(it -> {
            it.getMethods().stream().filter(MethodWrapper::hasInstructions)
                    .forEach(method -> {
                        InsnList instructions = method.getNode().instructions;

                        obfuscateConditions(method, instructions);
                        addFakeJumps(it, instructions);
                    });;
                    // make the field conditions
                    if (jumpIntCondField != null) {
                        it.addField(new FieldNode(ACC_PRIVATE | ACC_STATIC, jumpIntCondField, "I",
                                null, jumpIntCond));

                        jumpIntCondField = null;
                    }

                    if (jumpBoolCondField != null) {
                        it.addField(new FieldNode(ACC_PRIVATE | ACC_STATIC, jumpBoolCondField, "Z",
                                null, jumpBoolCond));

                        jumpBoolCondField = null;
                    }
        }
        );

    }

    private static void obfuscateConditions(MethodWrapper wrapper, InsnList instructions) {
        AbstractInsnNode insn = instructions.getFirst();

       /* do {
            int opcode = insn.getOpcode();

            if (opcode == IFEQ) {
                int var = ++wrapper.node.maxLocals;

                InsnList insns = new InsnList();

                insns.add(new InsnNode(ICONST_0));
                insns.add(new VarInsnNode(ISTORE, var));

                instructions.insert(insn, insns);
            }
        } while ((insn = insn.getNext()) != null);*/
    }

    /**
     * Replaces a jump instruction with a fake jump
     * @param wrapper The class wrapper
     * @param instructions The instructions of the method to obfuscate
     */
    private static void addFakeJumps(ClassWrapper wrapper, InsnList instructions) {
        AbstractInsnNode insn = instructions.getFirst();

        do {
            int opcode = insn.getOpcode();

            if (opcode == GOTO) {
                // we want to switch between comparing ints and booleans
                boolean useInt = ThreadLocalRandom.current().nextBoolean();

                if (useInt ? jumpIntCondField == null : jumpBoolCondField == null) {
                    if (useInt) {
                        jumpIntCondField = StringUtil.genName(10);

                        jumpIntCond = ThreadLocalRandom.current().nextInt();
                    } else {
                        jumpBoolCondField = StringUtil.genName(10);

                        jumpBoolCond = ThreadLocalRandom.current().nextBoolean();
                    }
                }

                InsnList jump = new InsnList();

                jump.add(new FieldInsnNode(GETSTATIC, wrapper.getNode().name, useInt ? jumpIntCondField : jumpBoolCondField, useInt ? "I" : "Z"));

                int jumpOpcode = useInt ? jumpIntCond < 0 ? IFLT : IFGE : jumpBoolCond ? IFNE : IFEQ;

                jump.add(new JumpInsnNode(jumpOpcode, ((JumpInsnNode) insn).label));

                jump.add(new InsnNode(ACONST_NULL));
                jump.add(new InsnNode(ATHROW));

                AbstractInsnNode last = jump.getLast();

                instructions.insert(insn, jump);
                instructions.remove(insn);

                insn = last;
            }
        } while ((insn = insn.getNext()) != null);
    }

}
