package me.iris.ambien.obfuscator.transformers.impl.flow;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.InstructionBuilder;
import me.iris.ambien.obfuscator.builders.InstructionModifier;
import me.iris.ambien.obfuscator.utilities.kek.myj2c.Myj2cASMUtils;
import me.iris.ambien.obfuscator.utilities.string.Namings;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.concurrent.ThreadLocalRandom;

import static me.iris.ambien.obfuscator.transformers.impl.flow.FlowObfuscationManager.classMethodsMap;

public class GotoFlow {


    private static String d;

    public GotoFlow() {
        // Transformer Loader will cause an NPE
        try {
            d = StringUtil.randomStringByNaming(8,
                    Ambien.get.theNamingNaming);
        } catch (Exception e) {
            e.printStackTrace();
            d = StringUtil.randomStringByNaming(8, Namings.iIl);
        }
    }

    public static void gotoFlow() {
        for (ClassWrapper classWrapper : classMethodsMap.keySet()) {
            transform(classWrapper.getNode());
        }
    }

    private static void transform(ClassNode node) {
        if (Myj2cASMUtils.isClassNodeInterface(node)) {
            return;
        }
        for (FieldNode why : node.fields) {
            d = StringUtil.randomStringByNaming(10, Namings.iIl);
        }

        String fieldName = d;
        boolean setupField = false;

        for (MethodNode method : node.methods) {
            if (method.instructions.size() == 0) continue;

            InstructionModifier modifier = new InstructionModifier();

            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof JumpInsnNode) {
                    JumpInsnNode jumpInsn = (JumpInsnNode) instruction;
                    if (jumpInsn.getOpcode() == Opcodes.GOTO) {
                        InstructionBuilder builder = new InstructionBuilder();
                        builder.fieldInsn(Opcodes.GETSTATIC, node.name, fieldName, "I");
                        builder.jump(Opcodes.IFLT, jumpInsn.label);

                        boolean oneShitLeft = false;
                        int randomInt = ThreadLocalRandom.current().nextInt(0, 5);
                        switch (randomInt) {
                            case 0:
                                builder.number(ThreadLocalRandom.current().nextInt());
                                oneShitLeft = true;
                                break;
                            case 1:
                                builder.ldc(ThreadLocalRandom.current().nextLong());
                                break;
                            case 2:
                                builder.insn(Opcodes.ACONST_NULL);
                                oneShitLeft = true;
                                break;
                            case 3:
                                builder.ldc(ThreadLocalRandom.current().nextFloat());
                                oneShitLeft = true;
                                break;
                            case 4:
                                builder.ldc(ThreadLocalRandom.current().nextDouble());
                                break;
                        }

                        if (oneShitLeft) {
                            builder.insn(Opcodes.POP);
                        } else {
                            builder.insn(Opcodes.POP2);
                        }

                        builder.insn(Opcodes.ACONST_NULL);
                        builder.insn(Opcodes.ATHROW);

                        modifier.replace(jumpInsn, builder.getList());
                        setupField = true;
                    }
                } else if (instruction instanceof VarInsnNode) {
                    VarInsnNode varInsn = (VarInsnNode) instruction;
                    int opcode = varInsn.getOpcode();

                    // Handle LOAD instructions
                    if (opcode == Opcodes.ILOAD || opcode == Opcodes.LLOAD ||
                            opcode == Opcodes.FLOAD || opcode == Opcodes.DLOAD ||
                            opcode == Opcodes.ALOAD) {

                        LabelNode label = new LabelNode();
                        int localVarIncrement = (opcode == Opcodes.LLOAD || opcode == Opcodes.DLOAD) ? 2 : 1;
                        method.maxLocals += localVarIncrement;

                        int index = method.maxLocals;

                        InstructionBuilder builder = new InstructionBuilder();
                        builder.varInsn(opcode + 33, index);
                        builder.varInsn(opcode, index);
                        builder.fieldInsn(Opcodes.GETSTATIC, node.name, fieldName, "I");
                        builder.jump(Opcodes.IFLT, label);

                        // Useless call to System.exit
                        // System.exit(blahBlahBlahRandomIntegerBlahBlahBlah);
                        // LDC a random integer
                        builder.ldc((int)ThreadLocalRandom.current().nextLong());
                        // Call to Runtime.getRuntime().halt(I)V
                        builder.methodInsn(Opcodes.INVOKESTATIC, "java/lang" +
                                        "/Runtime",
                                "getRuntime", "()Ljava/lang/Runtime;", false);

                        // 压入 0
                        builder.insn(Opcodes.ICONST_0);

                        // 调用 halt(0)
                        builder.methodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "halt",
                                "(I)V", false);

                        // Throw nothing
                        builder.insn(Opcodes.ACONST_NULL);
                        builder.insn(Opcodes.ATHROW);
                        // Build the instruction list
                        builder.label(label);

                        // Append the instruction list to the modifier
                        modifier.append(varInsn, builder.getList());
                        setupField = true;
                    }

                    // Handle STORE instructions
                    if (opcode == Opcodes.ISTORE || opcode == Opcodes.LSTORE ||
                            opcode == Opcodes.FSTORE || opcode == Opcodes.DSTORE ||
                            opcode == Opcodes.ASTORE) {

                        InstructionBuilder builder = new InstructionBuilder();
                        builder.varInsn(opcode - 33, varInsn.var);

                        // For double and long store, we use pop2
                        if (opcode == Opcodes.DSTORE || opcode == Opcodes.LSTORE) {
                            builder.insn(Opcodes.POP2);
                        } else {
                            builder.insn(Opcodes.POP);
                        }

                        modifier.append(varInsn, builder.getList());
                    }
                }
            }

            // apply
            modifier.apply(method);
        }

        if (setupField) {
            FieldNode field =
                    new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                            fieldName, "I", null,
                            ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, 0));
            node.fields.add(field);
        }
        d = StringUtil.randomStringByNaming(10, Ambien.get.theNamingNaming);
    }
}