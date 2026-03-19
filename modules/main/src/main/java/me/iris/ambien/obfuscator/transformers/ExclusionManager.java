package me.iris.ambien.obfuscator.transformers;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.utilities.kek.colonial.NodeUtils;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExclusionManager {
    private static final String
            EXCLUDE_DESC = "Lme/iris/library/annotations/Exclude;",
            FORCE_OBFUSCATE = "Lme/iris/library/annotations/ForceObfuscate;";
    private static final String AMBIEN_ANNOTATION_PREFIX = "Lme/iris/library/annotations/";

    // Exclude class prefix
    private final List<String> EXCLUDE_PREFIX;
    private final Map<String, String> excludedMethods;
    private final Map<String, List<String>> transformerClassExclusions;

    public ExclusionManager(JarWrapper wrapper) {
        this.EXCLUDE_PREFIX = new ArrayList<>();
        this.excludedMethods = new TreeMap<>();
        this.transformerClassExclusions = new TreeMap<>();
//        this.excludedPrefixes = new ArrayList<String>();

        // Add excluded classes that were defined in the config
        EXCLUDE_PREFIX.addAll(Ambien.get.excludedClasses);
//        excludedPrefixes.addAll(Ambien.get.excludedPrefixes);

        // Build a map of transformers and their excluded classes
        Ambien.get.transformerManager.getTransformers().forEach(transformer -> {
            final List<String> exclusions = transformer.excludedClasses.getOptions();
            if (!exclusions.isEmpty())
                transformerClassExclusions.put(transformer.getName(), exclusions);
        });

// Check for exclusions
        wrapper.getClasses().forEach(classWrapper -> {
            boolean isClassExcluded = NodeUtils.hasAnnotation(classWrapper.getNode().invisibleAnnotations, EXCLUDE_DESC);

            if (isClassExcluded) {
                EXCLUDE_PREFIX.add(classWrapper.getName());
            } else {
                classWrapper.getMethods().forEach(methodWrapper -> {
                    if (NodeUtils.hasAnnotation(methodWrapper.getNode().invisibleAnnotations, EXCLUDE_DESC)) {
                        excludedMethods.put(classWrapper.getName(), methodWrapper.getNode().name);
                    }
                });
            }
        });
    }


    /**
     * <h1>Proprietary API, does NOT handle force obfuscate annotation, use with caution!</h1>
     */
    private boolean isClassExcluded(String transformerName, String className) {
        // Check global list
        if (EXCLUDE_PREFIX.stream().anyMatch(className::startsWith)) {
            return true;
        }

        // Check specific list
        List<String> specific = transformerClassExclusions.get(transformerName);
        return specific != null && specific.stream().anyMatch(className::startsWith);
    }
    public boolean isClassExcluded(String transformerName, ClassNode node) {
        if(doForceObfuscate(node, transformerName)) {
            return false;
        }
        return isClassExcluded(transformerName, node.name);
    }

    /**
     * @see me.iris.library.annotations.ForceObfuscate#value()
     */
    @SuppressWarnings("JavadocReference")
    private boolean doForceObfuscate(@NotNull ClassNode node, @NotNull String transformerNameOrAll) {
        @Nullable
        AnnotationNode an = node.a114$getInvisibleAnnotation(FORCE_OBFUSCATE);
        if(an == null){
            return false;
        }
        @Nullable
        List<String> transformers = an.a114$getArrayValue("value");
        if(transformers == null || transformers.isEmpty()){
            return true;
        }
        if (transformerNameOrAll.equalsIgnoreCase("ALL")) {
            return true;
        }
        return transformers.stream()
                .anyMatch(it -> it.equals(transformerNameOrAll));
    }


    /**
     * <h1>Proprietary API, does NOT handle force obfuscate annotation, use with caution!</h1>
     * <br>
     * Checks if a method is excluded based on the class name and method name.
     * <br>
     * If the class contains the Exclude annotation, all methods in that class are excluded.
     * @param ownerClassName the name of the class that owns the method
     * @param methodName the name of the method to check
     * @return true if the method is excluded, false otherwise
     */
    @ApiStatus.Internal
    public boolean isMethodExcluded(final String ownerClassName, final String methodName) {

        if (excludedMethods.containsKey(ownerClassName)) {
            return excludedMethods.get(ownerClassName)
                    .contains(methodName);
        }

        return false;
    }


    public boolean isMethodExcluded(String ownerClassName, MethodNode methodNode) {
        if(methodNode.a114$hasInvisibleAnnotation(FORCE_OBFUSCATE)) {
            return false;
        }
        return isMethodExcluded(ownerClassName, methodNode.name);
    }

    public void removeAmbienAnnotations(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            // Remove on class
            removeAmbienAnnotationsFromList(classWrapper.getNode().invisibleAnnotations);

            // Remove on method
            classWrapper.getMethods().forEach(methodWrapper ->
                    removeAmbienAnnotationsFromList(methodWrapper.getNode().invisibleAnnotations)
            );
        });
    }

    private void removeAmbienAnnotationsFromList(List<AnnotationNode> annotations) {
        if (annotations != null && !annotations.isEmpty()) {
            annotations.removeIf(annotation -> annotation.desc.contains(AMBIEN_ANNOTATION_PREFIX));
        }
    }
}
