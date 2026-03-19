package me.iris.ambien.obfuscator.asm;

import me.iris.ambien.obfuscator.Ambien;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;

import java.util.List;
// WARNING: buggy!
public class CompetentClassWriter extends ClassWriter {

    // You need access to the list of classes being processed.
    // Pass this in via constructor or access it statically from your Ambien main class.
    private final List<ClassWrapper> classes;

    public CompetentClassWriter(final int flags/*, List<ClassWrapper> classes*/) {
        super(flags);
//        this.classes = classes;
        this.classes = Ambien.get.getJarWrapper().getClasses();
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        // 1. Try to load normally (handles JDK classes like String, ArrayList, etc.)
        ClassLoader classLoader = getClassLoader();
        Class<?> c, d;
        try {
            c = Class.forName(type1.replace('/', '.'), false, classLoader);
            d = Class.forName(type2.replace('/', '.'), false, classLoader);

            if (c.isAssignableFrom(d)) return type1;
            if (d.isAssignableFrom(c)) return type2;
            if (c.isInterface() || d.isInterface()) return "java/lang/Object";

            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');

        } catch (Exception e) {
            // 2. If Class.forName fails, it means the type is inside the JAR we are obfuscating
            // or a missing dependency. We must calculate hierarchy manually using ClassNodes.
            return getCommonSuperClassManual(type1, type2);
        }
    }

    private String getCommonSuperClassManual(String type1, String type2) {
        String first = type1;
        String second = type2;

        if (isAssignableFrom(first, second)) return first;
        if (isAssignableFrom(second, first)) return second;

        // Walk up the tree of 'first' until we find a parent that 'second' is assignable to
        do {
            first = getSuperClass(first);
            // If we reach the top or run out of info, return Object to prevent crash
            if (first == null || first.equals("java/lang/Object")) return "java/lang/Object";
        } while (!isAssignableFrom(first, second));

        return first;
    }

    // Helper: Check if type1 is a superclass of type2
    private boolean isAssignableFrom(String type1, String type2) {
        if (type1.equals("java/lang/Object")) return true;
        if (type1.equals(type2)) return true;

        String current = type2;
        while (current != null && !current.equals("java/lang/Object")) {
            if (current.equals(type1)) return true;
            current = getSuperClass(current);
        }
        return false;
    }

    // Helper: Get superclass name from your ClassWrappers
    private String getSuperClass(String type) {
        // Check if the class exists in the input JAR
        ClassNode node = getNodeFromName(type);

        if (node != null) {
            return node.superName;
        }

        // If not in input JAR, try to ask the JVM (for mixed cases)
        try {
            Class<?> c = Class.forName(type.replace('/', '.'), false, getClassLoader());
            if (c.getSuperclass() != null) {
                return c.getSuperclass().getName().replace('.', '/');
            }
        } catch (Exception ignored) {
            // If we can't find it anywhere, we assume Object to avoid crashing.
            // This is the "dirty fix" for missing dependencies (like msgpack).
        }
        return "java/lang/Object";
    }

    // Your existing logic (uncommented)
    private ClassNode getNodeFromName(final String path) {
        if (classes == null) return null;
        for (ClassWrapper wrapper : classes) {
            if (wrapper.getName().equals(path))
                return wrapper.getNode();
        }
        return null;
    }
}