package cn.a114.commonutil.extensions.org.objectweb.asm.tree.FieldNode;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

@Extension
public class FieldNodeExt {

    public static boolean a114$isMixin(@This FieldNode $this) {
        return $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/Final;")
                || $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/Mutable;")
                || $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/Shadow;")
                || $this.a114$hasAnnotation("Lorg/spongepowered/asm/mixin/Unique;");
    }

    public static boolean a114$hasAnnotation(@This FieldNode $this, String annotationDescriptor) {
        return $this.a114$hasInvisibleAnnotation(annotationDescriptor) || $this.a114$hasVisibleAnnotation(annotationDescriptor);
    }

    public static boolean a114$hasInvisibleAnnotation(@This FieldNode $this, String annotationDescriptor) {
        return $this.a114$getInvisibleAnnotation(annotationDescriptor) != null;
    }

    public static boolean a114$hasVisibleAnnotation(@This FieldNode $this, String annotationDescriptor) {
        return $this.a114$getVisibleAnnotation(annotationDescriptor) != null;
    }

    public static AnnotationNode a114$getInvisibleAnnotation(@This FieldNode $this, String annotationDescriptor) {
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

    public static AnnotationNode a114$getVisibleAnnotation(@This FieldNode $this, String annotationDescriptor) {
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
