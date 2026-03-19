package me.iris.ambien.obfuscator.utilities.kek.myj2c;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;

// Shut up lombok
@SuppressWarnings({"SpellCheckingInspection", "LombokGetterMayBeUsed"})
public class Myj2cInsnBuilder {

    private final InsnList insnList;

    private Myj2cInsnBuilder() {
        this.insnList = new InsnList();
    }

    public Myj2cInsnBuilder insn(AbstractInsnNode... insnNodes) {
        Arrays.stream(insnNodes).forEach(this.insnList::add);
        return this;
    }

    public InsnList getInsnList() {
        return insnList;
    }

    public static Myj2cInsnBuilder createEmpty() {
        return new Myj2cInsnBuilder();
    }

    /**
     * Creates an InsnList that invokes String.length() and the result is popped
     * and ignored.
     *
     * @param s the string to invoke length on
     * @return an InsnList with the instructions to invoke String.length() and pop
     */
    public static InsnList stringLengthInvokeAndPop(String s) {
        InsnList insns = new InsnList();
        // ldc the string constant
        insns.add(new LdcInsnNode(s));
        // invoke the String.length() method
        insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));
        // ignore the result by popping it
        insns.add(new InsnNode(Opcodes.POP));
        return insns;
    }

}