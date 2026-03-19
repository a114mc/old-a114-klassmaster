package cn.a114.commonutil.extensions.org.objectweb.asm.tree.AnnotationNode;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;

@Extension
public class AnnotationNodeExt {

    /**
     * Retrieves values from a String[] annotation property.
     * Usage: List<String> values = myAnnotationNode.getArrayValue("value");
     */
    public static List<String> a114$getArrayValue(@This AnnotationNode $this, String valueName) {
        if ($this.values == null) return null;

        for (int i = 0; i < $this.values.size(); i += 2) {
            String name = (String) $this.values.get(i);
            if (name.equals(valueName)) {
                Object value = $this.values.get(i + 1);

                // ASM stores array values as a List
                if (value instanceof List) {
                    return (List<String>) value;
                }
            }
        }
        return null;
    }
}