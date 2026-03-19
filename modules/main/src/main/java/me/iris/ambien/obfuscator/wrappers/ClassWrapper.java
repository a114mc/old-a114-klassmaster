package me.iris.ambien.obfuscator.wrappers;

import lombok.Getter;
import lombok.Setter;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.asm.CompetentClassWriter;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodTooLargeException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@SuppressWarnings("CastCanBeRemovedNarrowingVariableType")
public class ClassWrapper implements Opcodes {
    @Getter
    private String name;
    public void setName(String _name){
        this.name = _name;
        node.name = this.name;
    }
    @Getter
    private final List<MethodWrapper> methods;
    @Getter
    private final List<MethodNode> realMethods;
    @Getter
    private final boolean isLibraryClass;


    @Getter
    @Setter
    private ClassNode node;

    public ClassWrapper(String name, ClassNode node, boolean isLibraryClass) {
        this.name = name;
        this.node = node;
        this.methods = new ArrayList<>();
        this.realMethods = new ArrayList<>();
        this.isLibraryClass = isLibraryClass;

        // Import methods from class
        Arrays.stream(node.methods.toArray())
                .map(MethodNode.class::cast)
                .forEach(methodNode -> {
                    methods.add(new MethodWrapper(methodNode));
                    realMethods.add(methodNode);
                });
    }

    public MethodNode getStaticInitializer() {
        // Check if the init method already exists in the class.
        for (MethodWrapper wrapper : methods) {
            if (wrapper.getNode().name.equals("<clinit>"))
                return wrapper.getNode();
        }

        // Create init method & return it
        final MethodBuilder builder = MethodBuilder
                .builder()
                .access(ACC_STATIC)
                .name("<clinit>")
                .desc("()V")
                .build();
        final MethodNode methodNode = builder.buildNode();

        // Add return insn
        methodNode.instructions.add(new InsnNode(RETURN));

        // Add method to class
        addMethod(methodNode);

        // Return method
        return methodNode;
    }

    public List<MethodWrapper> getTransformableMethods() {
        return methods.stream()
                .filter(method -> !Ambien.exclusionManager.isMethodExcluded(name, method.getNode()))
                .collect(Collectors.toList());
    }
    public List<MethodNode> getTransformableMethodNodes() {
        return realMethods.stream()
                .filter(method -> !Ambien.exclusionManager.isMethodExcluded(name, method))
                .collect(Collectors.toList());
    }

    public boolean isInterface() {
        return (node.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
    }

    public boolean isEnum() {
        return (node.access & Opcodes.ACC_ENUM) == Opcodes.ACC_ENUM;
    }


    public boolean isAnnotation() {
        return (node.access & Opcodes.ACC_ANNOTATION) == Opcodes.ACC_ANNOTATION;
    }

    public CopyOnWriteArrayList<FieldNode> getFields() {
        final CopyOnWriteArrayList<FieldNode> fields = new CopyOnWriteArrayList<>();
        for (final Object fieldObj : node.fields) {
            fields.add((FieldNode) fieldObj);
        }

        return fields;
    }

    public void addField(final FieldNode fieldNode) {
        node.fields.add(fieldNode);
    }

    public void addMethod(final MethodNode methodNode) {
        node.methods.add(methodNode);
        methods.add(new MethodWrapper(methodNode));
    }

    // Bro that was dumb
    public byte[] toByteArray() {
        Ambien.logger.debug("Converting class to bytes: {}", name);

        try {
            // Attempt to get bytes of class using COMPUTE_FRAMES
            final CompetentClassWriter writer = new CompetentClassWriter(ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Exception e) {
//            e.printStackTrace();
            if (e instanceof NegativeArraySizeException) {
                Ambien.logger.warn("NegativeArraySizeException thrown when attempting to write class \"{}\" using COMPUTE_MAXS, this is most likely caused by malformed bytecode.", name);
            } else {
                e.printStackTrace();
                Ambien.logger.warn("Attempting to write class \"{}\" using COMPUTE_MAXS, some errors may appear during runtime.", name);
            }

            if(e instanceof MethodTooLargeException){
                // org.objectweb.asm.MethodTooLargeException: Method too large: com/ibm/icu/impl/LocaleFallbackData.buildDefaultScriptTable ()Ljava/util/Map;
                Ambien.logger.error("Method was too large!");
            }

            // Attempt to get bytes of class using COMPUTE_MAXS
            final CompetentClassWriter writer = new CompetentClassWriter(ClassWriter.COMPUTE_MAXS);
            try {
                node.accept(writer);
            } catch (IllegalArgumentException ex) {
//                throw new RuntimeException("Illegal argument??!!");
            } catch (NullPointerException sb){
                sb.printStackTrace();
            }
	        try {
		        return writer.toByteArray();
	        } catch (ArrayIndexOutOfBoundsException ex) {
		        throw new RuntimeException("Writer array index out of bound?");
	        } catch (MethodTooLargeException mes){
                Ambien.logger.warn("Method too large, trying with COMPUTE_MAXS + COMPUTE_FRAMES flags...");
                final CompetentClassWriter noDebugWriter = new CompetentClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

                return noDebugWriter.toByteArray();
            }
        }
    }
}
