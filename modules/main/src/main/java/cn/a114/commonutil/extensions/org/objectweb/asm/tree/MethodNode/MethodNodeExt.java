package cn.a114.commonutil.extensions.org.objectweb.asm.tree.MethodNode;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * 唉哟，manifold太好用了，再也不用为了extension method而写kotlin了。
 * @author a114
 */
@SuppressWarnings("DuplicatedCode")// Doesn't work after method extraction
@Extension
public class MethodNodeExt {

    public static boolean a114$isMixinReferenceTip(@This MethodNode $this) {
        // Defensive programming 2026
        return $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/Overwrite;")
                || $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/Shadow;")
                || $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/gen/Accessor;")
                || $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/gen/Invoker;");
    }

    public static boolean a114$hasAnnotation(@This MethodNode $this, String annotationDescriptor) {
        return $this.a114$hasInvisibleAnnotation(annotationDescriptor) || $this.a114$hasVisibleAnnotation(annotationDescriptor);
    }

    public static boolean a114$hasInvisibleAnnotation(@This MethodNode $this, String annotationDescriptor) {
        return $this.a114$getInvisibleAnnotation(annotationDescriptor) != null;
    }

    public static boolean a114$hasVisibleAnnotation(@This MethodNode $this, String annotationDescriptor) {
        return $this.a114$getVisibleAnnotation(annotationDescriptor) != null;
    }

    public static AnnotationNode a114$getInvisibleAnnotation(@This MethodNode $this, String annotationDescriptor) {
        if ($this == null || $this.invisibleAnnotations == null) {
            return null;
        }
        for (AnnotationNode it : $this.invisibleAnnotations) {
            if (it.desc.equals(annotationDescriptor)) {
                return it;
            }
        }
        return null;
    }

    public static AnnotationNode a114$getVisibleAnnotation(@This MethodNode $this, String annotationDescriptor) {
        if ($this == null || $this.visibleAnnotations == null) {
            return null;
        }
        for (AnnotationNode it : $this.visibleAnnotations) {
            if (it.desc.equals(annotationDescriptor)) {
                return it;
            }
        }
        return null;
    }
}
