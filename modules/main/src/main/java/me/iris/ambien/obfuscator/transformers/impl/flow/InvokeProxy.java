package me.iris.ambien.obfuscator.transformers.impl.flow;

import cn.a114.commonutil.random.ThreadLocalRandomManager;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.InstructionModifier;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.transformers.impl.exploits.SyntheticMarker;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import me.iris.ambien.obfuscator.utilities.kek.UnicodeDictionary;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@TransformerInfo(
        name = "invoke-proxy",
        category = Category.CONTROL_FLOW,
        stability = Stability.STABLE,
        ordinal = Ordinal.STANDARD,
        description = "asdcfasdfasdf."
)
public class InvokeProxy extends Transformer {

    public static BooleanSetting anti_EpicPlayerA10 =
            new BooleanSetting("anti-narumii-deobfuscator",true);

    @Override
    public void transform(JarWrapper wrapper) {
        getClasses(wrapper)
                .forEach(classWrapper -> invokeProxy(classWrapper.getNode()));
    }

    public void invokeProxy(ClassNode node) {
        if (Modifier.isInterface(node.access)) return;

        List<MethodNode> syntheticMethods = new ArrayList<>();
        UnicodeDictionary dictionary = new UnicodeDictionary(2);

        for (MethodNode method : node.methods) {
            dictionary.addUsed(method.name);
        }

        for (MethodNode method : node.methods) {
            InstructionModifier modifier = new InstructionModifier();

            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) instruction;
                    switch (methodInsn.getOpcode()) {
                        case INVOKESTATIC: {
                            String methodName = dictionary.get();
                            MethodNode methodNode = new MethodNode(
                                    ACC_PRIVATE | ACC_STATIC,
                                    methodName,
                                    methodInsn.desc,
                                    null,
                                    null
                            );
                            antiNarumiiDeobf(methodNode, dictionary);

                            Type returnType = Type.getReturnType(methodInsn.desc);

                            visitArgs(0, Type.getArgumentTypes(methodInsn.desc), methodNode);
                            methodNode.visitMethodInsn(
                                    INVOKESTATIC,
                                    methodInsn.owner,
                                    methodInsn.name,
                                    methodInsn.desc,
                                    methodInsn.itf
                            );
                            visitReturn(returnType, methodNode);

                            syntheticMethods.add(methodNode);
                            modifier.replace(
                                    instruction,
                                    new MethodInsnNode(
                                            INVOKESTATIC,
                                            node.name,
                                            methodName,
                                            methodInsn.desc,
                                            false
                                    )
                            );

                            
                            break;
                        }
                        case INVOKEVIRTUAL: {
                            Type[] types = Type.getArgumentTypes(methodInsn.desc);
                            Type[] desc = new Type[types.length + 1];
                            desc[0] = Type.getObjectType(methodInsn.owner);
                            System.arraycopy(types, 0, desc, 1, types.length);

                            String methodName = dictionary.get();
                            Type returnType = Type.getReturnType(methodInsn.desc);
                            String methodDesc = Type.getMethodDescriptor(returnType, desc);
                            MethodNode methodNode = new MethodNode(
                                    ACC_PRIVATE | ACC_STATIC,
                                    methodName,
                                    methodDesc,
                                    null,
                                    null
                            );

                            antiNarumiiDeobf(methodNode, dictionary);

                            methodNode.visitVarInsn(ALOAD, 0);
                            visitArgs(1, types, methodNode);
                            methodNode.visitMethodInsn(
                                    INVOKEVIRTUAL,
                                    methodInsn.owner,
                                    methodInsn.name,
                                    methodInsn.desc,
                                    methodInsn.itf
                            );
                            visitReturn(returnType, methodNode);

                            syntheticMethods.add(methodNode);
                            modifier.replace(
                                    instruction,
                                    new MethodInsnNode(
                                            INVOKESTATIC,
                                            node.name,
                                            methodName,
                                            methodDesc,
                                            false
                                    )
                            );

                            
                            break;
                        }
                    }
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) instruction;
                    switch (fieldInsn.getOpcode()) {
                        case GETSTATIC: {
                            Type type = Type.getType(fieldInsn.desc);
                            String methodDescriptor = Type.getMethodDescriptor(type);
                            String methodName = dictionary.get();
                            MethodNode methodNode = new MethodNode(
                                    ACC_PRIVATE | ACC_STATIC,
                                    methodName,
                                    methodDescriptor,
                                    null,
                                    null
                            );

                            antiNarumiiDeobf(methodNode, dictionary);

                            methodNode.visitFieldInsn(
                                    GETSTATIC,
                                    fieldInsn.owner,
                                    fieldInsn.name,
                                    fieldInsn.desc
                            );
                            visitReturn(type, methodNode);

                            syntheticMethods.add(methodNode);
                            modifier.replace(
                                    instruction,
                                    new MethodInsnNode(
                                            INVOKESTATIC,
                                            node.name,
                                            methodName,
                                            methodDescriptor,
                                            false
                                    )
                            );

                            
                            break;
                        }
                        case PUTSTATIC: {
                            Type type = Type.getType(fieldInsn.desc);
                            String methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, type);
                            String methodName = dictionary.get();
                            MethodNode methodNode = new MethodNode(
                                    ACC_PRIVATE | ACC_STATIC,
                                    methodName,
                                    methodDescriptor,
                                    null,
                                    null
                            );

                            visitArgs(0, new Type[]{type}, methodNode);
                            methodNode.visitFieldInsn(
                                    PUTSTATIC,
                                    fieldInsn.owner,
                                    fieldInsn.name,
                                    fieldInsn.desc
                            );
                            methodNode.visitInsn(RETURN);

                            syntheticMethods.add(methodNode);
                            modifier.replace(
                                    instruction,
                                    new MethodInsnNode(
                                            INVOKESTATIC,
                                            node.name,
                                            methodName,
                                            methodDescriptor,
                                            false
                                    )
                            );

                            
                            break;
                        }
                        case GETFIELD: {
                            if (!method.name.equals("<init>")) {
                                Type type = Type.getType(fieldInsn.desc);
                                Type objectType = Type.getObjectType(fieldInsn.owner);
                                String methodDescriptor = Type.getMethodDescriptor(type, objectType);
                                String methodName = dictionary.get();
                                MethodNode methodNode = new MethodNode(
                                        ACC_PRIVATE | ACC_STATIC,
                                        methodName,
                                        methodDescriptor,
                                        null,
                                        null
                                );

                                visitArgs(0, new Type[]{objectType}, methodNode);
                                methodNode.visitFieldInsn(
                                        GETFIELD,
                                        fieldInsn.owner,
                                        fieldInsn.name,
                                        fieldInsn.desc
                                );
                                visitReturn(type, methodNode);

                                syntheticMethods.add(methodNode);
                                modifier.replace(
                                        instruction,
                                        new MethodInsnNode(
                                                INVOKESTATIC,
                                                node.name,
                                                methodName,
                                                methodDescriptor,
                                                false
                                        )
                                );

                                
                            }
                            break;
                        }
                        case PUTFIELD: {
                            if (!method.name.equals("<init>")) {
                                Type type = Type.getType(fieldInsn.desc);
                                Type objectType = Type.getObjectType(fieldInsn.owner);
                                String methodDescriptor = Type.getMethodDescriptor(
                                        Type.VOID_TYPE,
                                        objectType,
                                        type
                                );
                                String methodName = dictionary.get();
                                MethodNode methodNode = new MethodNode(
                                        ACC_PRIVATE | ACC_STATIC,
                                        methodName,
                                        methodDescriptor,
                                        null,
                                        null
                                );

                                visitArgs(0, new Type[]{objectType, type}, methodNode);
                                methodNode.visitFieldInsn(
                                        PUTFIELD,
                                        fieldInsn.owner,
                                        fieldInsn.name,
                                        fieldInsn.desc
                                );
                                methodNode.visitInsn(RETURN);

                                syntheticMethods.add(methodNode);
                                modifier.replace(
                                        instruction,
                                        new MethodInsnNode(
                                                INVOKESTATIC,
                                                node.name,
                                                methodName,
                                                methodDescriptor,
                                                false
                                        )
                                );

                                
                            }
                            break;
                        }
                    }
                }
            }

            modifier.apply(method);
        }

        for (MethodNode syntheticMethod : syntheticMethods) {
            GOTOASMUtils.computeMaxLocals(syntheticMethod);
        }

        node.methods.addAll(syntheticMethods);
    }

    private void visitArgs(int offset, Type[] types, MethodNode methodNode) {
        int index = offset;

        for (Type type : types) {
            if (type == Type.INT_TYPE || type == Type.SHORT_TYPE || type == Type.BYTE_TYPE ||
                    type == Type.CHAR_TYPE || type == Type.BOOLEAN_TYPE) {
                methodNode.visitVarInsn(ILOAD, index);
            } else if (type == Type.LONG_TYPE) {
                methodNode.visitVarInsn(LLOAD, index);
            } else if (type == Type.FLOAT_TYPE) {
                methodNode.visitVarInsn(FLOAD, index);
            } else if (type == Type.DOUBLE_TYPE) {
                methodNode.visitVarInsn(DLOAD, index);
            } else {
                methodNode.visitVarInsn(ALOAD, index);
            }

            if (type == Type.DOUBLE_TYPE || type == Type.LONG_TYPE) {
                index += 2;
            } else {
                index++;
            }
        }
    }

    private void visitReturn(Type type, MethodNode methodNode) {
        if (type.getSort() == Type.METHOD) {
            methodNode.visitInsn(RETURN);
        } else {
            switch (type.getSort()) {
                case Type.VOID:
                    methodNode.visitInsn(RETURN);
                    break;
                case Type.INT:
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.SHORT:
                case Type.BYTE:
                    // Integer return LMAO
                    methodNode.visitInsn(IRETURN);
                    break;
                case Type.FLOAT:
                    methodNode.visitInsn(FRETURN);
                    break;
                case Type.DOUBLE:
                    methodNode.visitInsn(DRETURN);
                    break;
                case Type.LONG:
                    methodNode.visitInsn(LRETURN);
                    break;
                default:
                    // Any returns
                    methodNode.visitInsn(ARETURN);
                    break;
            }
        }
    }

    // 未雨绸缪防止被deobf
    /**
     * Add access first, if anti deobf enabled it will add useless call like
     * <pre><code>
     * Math.max("","IDK");
     * </code></pre>
     * Which is a nice way to prevent deobfuscating
     */
    public static void antiNarumiiDeobf (final MethodNode methodNode,
                                         UnicodeDictionary dictionary){
        if(Ambien
                   .get
                   .transformerManager
                   .getTransformer("sb-marker")
                   .isEnabled()
           &&
           SyntheticMarker.METHOD_SYNTHETIC.isEnabled()
        ){
            GOTOASMUtils.addAccess(methodNode, ACC_SYNTHETIC);
        }

        if(Ambien
                   .get
                   .transformerManager
                   .getTransformer("sb-marker")
                   .isEnabled()
           &&
           SyntheticMarker.METHOD_BRIDGE.isEnabled()
        ){
            GOTOASMUtils.addAccess(methodNode, ACC_BRIDGE);
        }

        if(!anti_EpicPlayerA10.isEnabled()){
            return;
        }

        // This will f EpicPlayerA10's code in the ass
        // https://github.com/narumii/Deobfuscator/
        // STACK #0
        methodNode.visitLdcInsn("");
        methodNode.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/String",
                "length",
                "()I",
                false
                                  );
        // STACK #1
        // Random string
        methodNode.visitLdcInsn(dictionary.get());
        // Length call
        methodNode.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/String",
                "length",
                "()I",
                false
                                  );
        // Call random sh1t🤣
        methodNode.visitMethodInsn(
                INVOKESTATIC,
                "java/lang/Math",
                getRandomMathMethodName(),
                "(II)I",
                false
                                  );
        methodNode.visitInsn(POP);
    }
    
    // return a random method's name in java/lang/Math with signature (II)I
    
    public static String getRandomMathMethodName() {
        List<String> methods = new ArrayList<>();
        methods.add("max");
        methods.add("min");
        methods.add("addExact");
        methods.add("subtractExact");
        methods.add("multiplyExact");
        methods.add("floorDiv");
        methods.add("floorDiv");
        methods.add("floorMod");
        return methods.get(ThreadLocalRandomManager.theThreadLocalRandom.nextInt(methods.size()));
    }
}
