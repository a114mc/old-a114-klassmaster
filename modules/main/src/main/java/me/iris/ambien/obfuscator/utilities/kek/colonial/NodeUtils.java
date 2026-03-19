package me.iris.ambien.obfuscator.utilities.kek.colonial;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

// Superblaubeere27 obf skidding? I'm not sure...
public class NodeUtils {
    @Nullable
    public static MethodNode getMethod(final ClassNode classNode, final String name) {
        for (final MethodNode method : classNode.methods)
            if (method.name.equals(name))
                return method;
        return null;
    }
    // 工具方法：判断是否存在指定注解
    public static boolean hasAnnotation(List<AnnotationNode> annotations, String desc) {
        if (annotations == null || annotations.isEmpty()) return false;
        return annotations.stream().anyMatch(annotation -> annotation.desc.contains(desc));
    }
}
