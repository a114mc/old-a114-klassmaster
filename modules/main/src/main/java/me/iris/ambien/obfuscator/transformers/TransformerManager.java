/*
 * Bruhfuscator-CN
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.transformers;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings("LombokGetterMayBeUsed")
public class TransformerManager {

    private final List<Transformer> transformers;

    public TransformerManager() {
        this.transformers = new ArrayList<>();

        // 假设 findClassesAnnotatedWith 方法能够返回被 TransformerInfo 注解的 Class 对象
        List<Class<?>> sb = null;
        try {
            sb = findClassesAnnotatedWith("me.iris.ambien.obfuscator.transformers.impl", TransformerInfo.class);
        } catch (Throwable e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            throw new NoClassDefFoundError("B?!");
        }

        // 遍历每个 Class 对象，创建实例并添加到 transformers 列表中
        for (Class<?> clazz : sb) {
            try {
                // 检查这个 Class 是否可以被赋值给 Transformer 类型
                if (Transformer.class.isAssignableFrom(clazz)) {
                    // 创建类的实例
                    // 确保您的 Transformer 类有一个无参构造函数
                    Transformer transformer = (Transformer) clazz.getDeclaredConstructor().newInstance();
                    this.transformers.add(transformer);
                    Ambien.logger.debug("Added transformer: " + transformer.getName());
                } else {
                    Ambien.logger.error("What the f? Class "
                            + clazz.getName()
                            + " was annotated with TransformerInfo but does not implements or extends Transformer."
                    );
                }
            } catch (Exception e) {
                // 处理实例化过程中可能出现的异常，例如：
                // InstantiationException (如果类是抽象的或接口)
                // IllegalAccessException (如果构造函数不可访问)
                // NoSuchMethodException (如果缺少无参构造函数)
                // InvocationTargetException (如果构造函数内部抛出异常)
                Ambien.logger.error("Failed to instantiate transformer class: " + clazz.getName() + " -> " + e.getMessage());
                Ambien.logger.error("Stacktrace was printed.");
                e.printStackTrace(); // 打印完整的堆栈跟踪以进行调试
            }
        }

        // Sort transformers by ordinal
        transformers.sort(Comparator.comparingInt(transformer -> -transformer.getOrdinal().getIdx()));
    }

    public static List<Class<?>> findClassesAnnotatedWith(String packageName, Class<? extends Annotation> annotationClass) throws ClassNotFoundException {
        List<Class<?>> foundClasses = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("jar")) {
                    String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!")); // 去掉 "file:" 前缀
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String entryName = entry.getName();
                            if (entryName.endsWith(".class") && entryName.startsWith(path)) {
                                String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                                Class<?> cls = Class.forName(className);
                                if (cls.isAnnotationPresent(annotationClass)) {
                                    foundClasses.add(cls);
                                }
                            }
                        }
                    }
                }
                else {
                    File directory = new File(resource.getFile());

                    if (directory.exists() && directory.isDirectory()) {
                        findClassesInDirectory(directory, packageName, annotationClass, foundClasses);
                    }
                }
            }
        } catch (IOException e) {
            Ambien.logger.error("Failed to read package: " + packageName);
            e.printStackTrace();
        }
        return foundClasses;
    }

    private static void findClassesInDirectory(File directory, String packageName,
                                               Class<? extends Annotation> annotationClass,
                                               List<Class<?>> foundClasses) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + "." + file.getName(), annotationClass, foundClasses);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> cls = Class.forName(className);
                    if (cls.isAnnotationPresent(annotationClass)) {
                        foundClasses.add(cls);
                    }
                } catch (ClassNotFoundException e) {
                    Ambien.logger.error("Could not load class: " + className);
                } catch (NoClassDefFoundError e) {
                    // Sometimes dependencies may be missing; skip these classes
                }
            }
        }
    }
    public List<Transformer> getTransformers() {
        return transformers;
    }

    @Nullable
    public Transformer getTransformer(String name) {
        for (Transformer transformer : transformers) {
            if (transformer.getName().equals(name))
                return transformer;
        }
        throw new RuntimeException(new ClassNotFoundException("No transformer found with name " + name));
    }
}
