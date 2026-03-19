package cn.a114.commonutil.extensions.org.objectweb.asm.tree.ClassNode;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;


/**
 * 唉哟，manifold太好用了，再也不用为了extension method而写kotlin了。
 *
 * @author a114
 */
@SuppressWarnings({"DuplicatedCode", "unused"})// Doesn't work after method extraction
@Extension
public class ClassNodeExt {
    public static boolean a114$isMixin(@This ClassNode $this) {
        // Defensive programming 2026
        return $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/Mixin;");
    }
    public static boolean a114$hasAnnotation(@This ClassNode $this, String annotationDescriptor) {
        return $this.a114$hasInvisibleAnnotation(annotationDescriptor) || $this.a114$hasVisibleAnnotation(annotationDescriptor);
    }

    public static boolean a114$hasInvisibleAnnotation(@This ClassNode $this, String annotationDescriptor) {
        return $this.a114$getInvisibleAnnotation(annotationDescriptor) != null;
    }

    public static boolean a114$hasVisibleAnnotation(@This ClassNode $this, String annotationDescriptor) {
        return $this.a114$getVisibleAnnotation(annotationDescriptor) != null;
    }

    public static AnnotationNode a114$getInvisibleAnnotation(@This ClassNode $this, String annotationDescriptor) {
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

    public static AnnotationNode a114$getVisibleAnnotation(@This ClassNode $this, String annotationDescriptor) {
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
