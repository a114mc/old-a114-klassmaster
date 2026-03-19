package me.iris.ambien.obfuscator.wrappers;

import cn.a114.commonutil.j8.StringRepeat;
import cn.a114.commonutil.random.ThreadLocalRandomManager;
import cn.a114.commonutil.wtf.CrazyStuff;
import lombok.Getter;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.builders.MethodBuilder;
import me.iris.ambien.obfuscator.transformers.TransformerManager;
import me.iris.ambien.obfuscator.transformers.impl.data.StringEncryptionManager;
import me.iris.ambien.obfuscator.transformers.impl.exploits.Crasher;
import me.iris.ambien.obfuscator.transformers.impl.packaging.DuplicateResources;
import me.iris.ambien.obfuscator.transformers.impl.packaging.FolderClasses;
import me.iris.ambien.obfuscator.transformers.impl.packaging.Metadata;
import me.iris.ambien.obfuscator.transformers.impl.packaging.RemapTransformer;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

import static me.iris.ambien.obfuscator.transformers.impl.data.string.AllatoriLikeStringEncryption.*;

@SuppressWarnings({"all","removal"})
public class JarWrapper {
    @Getter
    private final List<String> directories;

    @Getter
    private final List<ClassWrapper> classes;

    @Getter
    private final HashMap<String, byte[]> resources;

    @Getter
    private final List<ByteArrayOutputStream> outputStreams;

    public JarWrapper() {
        this.directories = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.resources = new HashMap<>();
        this.outputStreams = new ArrayList<>();
    }

    public JarWrapper from(final File file) throws IOException {
        String p = file.getAbsolutePath();
        if (!file.exists()) {
            Ambien.logger.error("Input jar file (" + p + ") does not exist.");
            CrazyStuff.halt(0);
        }

        if (!file.getName().endsWith(".jar")) {
            Ambien.logger.error(
                    "Input file"
                    + p
                    + " doesn't have a .jar suffix, make sure it's a jar file.");
            CrazyStuff.halt(0);
        }

        // Convert file to jar file
        final JarFile jarFile = new JarFile(file);
        Ambien.logger.info("Loading jar: " + jarFile.getName());

        // Get jar file entries
        final Enumeration<JarEntry> entries = jarFile.entries();

        // Enumerate
        while (entries.hasMoreElements()) {
            // Get element
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();
            final InputStream stream = jarFile.getInputStream(entry);

            // Load entry
            if (name.endsWith(".class")) {
                // Read stream into node
                final ClassReader reader = new ClassReader(stream);
                final ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.EXPAND_FRAMES);

                classes.add(new ClassWrapper(node.name, node, false));
                Ambien.logger.debug("Loaded class: {}", name);
            } else if (name.endsWith("/")) {
                directories.add(name);
            }
            else {
                final byte[] bytes = IOUtil.streamToArray(stream);
                resources.put(name, bytes);
                Ambien.logger.debug("Loaded resource: {}", name);
            }
        }

        // Return wrapper
        return this;
    }

    public JarWrapper importLibrary(final String path) throws IOException {
        final File file = new File(path);

        if (!file.exists())
            throw new RuntimeException(String.format("Library \"%s\" doesn't exist.", path));

        if (file.isDirectory()) {
            File[] jarFiles = file.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles == null) {
                throw new RuntimeException(String.format("No .jar files found in directory \"%s\".", path));
            }

            for (File jarFile : jarFiles) {
                importJar(jarFile);
            }
        } else if (file.getName().endsWith(".jar")) {
            importJar(file);
        } else {
            throw new RuntimeException(String.format("Library \"%s\" isn't a .jar file or a directory.", path));
        }

        return this;
    }

    private void importJar(File jarFile) throws IOException {
        final JarFile jar = new JarFile(jarFile);
        Ambien.logger.info("Loading library: " + jar.getName());

        final Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String name = entry.getName();
            final InputStream stream = jar.getInputStream(entry);

            if (name.endsWith(".class")
                // Why
                /* || name.endsWith(".class/")*/
            ) {
                final ClassReader reader = new ClassReader(stream);
                final ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.EXPAND_FRAMES);

                classes.add(new ClassWrapper(name, node, true));
                Ambien.logger.debug("Loaded library class: {}", name);
            }
        }
    }

    public String to() throws IOException {
        // File writer for all our output streams
        String oj = Ambien.get.outputJar;
//      Object sb = Ambien.get.
        final FileOutputStream fileOutputStream = new FileOutputStream(oj);

        if (!outputStreams.isEmpty()) {
            for (ByteArrayOutputStream outputStream : outputStreams) {
                fileOutputStream.write(outputStream.toByteArray());
            }

            Ambien.logger.debug("Added {} extra output streams", outputStreams.size());
        }

        // Write main jar stream
        // Create output stream
        final JarOutputStream stream = new JarOutputStream(fileOutputStream);

        // Set compression level
        if (Ambien.get.transformerManager.getTransformer("aggressive-compression").isEnabled())
        {
            stream.setLevel(Deflater.BEST_COMPRESSION);
        }
        processBoom(stream);

//      操你妈的不要动我的压缩等级
//      else {
//          stream.setLevel(Deflater.DEFAULT_STRATEGY);
//      }

        // Add directories
        directories.forEach(directory -> {
            try {
                IOUtil.writeDirectoryEntry(stream, directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Add resources
        resources.forEach((name, bytes) -> {
            try {
                TransformerManager t = Ambien.get.transformerManager;
                {
                    if (
                            t.getTransformer("folder-classes").isEnabled()
                                    &&
                                    FolderClasses.folderResources.isEnabled()
                    ) {
                        IOUtil.writeEntry(stream, name + "/", bytes);
                    } else IOUtil.writeEntry(stream, name, bytes); // "resource.yml"
                }

                if (
                        t.getTransformer("duplicate-resources").isEnabled()
                                &&
                                DuplicateResources.dupResources.isEnabled()
                ) {
                    int dupAmount = DuplicateResources.dupAmount.getValue();
                    for (int x = 1; x <= dupAmount; x++) {
                        String modifiedName = name + StringRepeat.repeat("\u0000", x);
                        byte[] duplicatedData = IOUtil.duplicateData(bytes);
                        IOUtil.writeEntry(stream, modifiedName, duplicatedData); // "resource.yml  "
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Add classes
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicVerifier());
        classes.stream()
                .filter(classWrapper -> !classWrapper.isLibraryClass())
                .forEach(classWrapper -> {
                    classWrapper.getNode().methods.forEach(methodNode -> {
                        try {
                            analyzer.analyzeAndComputeMaxs(methodNode.name, methodNode);
                        } catch (AnalyzerException e) {
                            e.addSuppressed(new Throwable("AnalyzerException occurred! at" + classWrapper.getNode().name + ":" + methodNode.name));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    try {
                        String name = classWrapper.getNode().name + ".class"; // "Class.class"
                        String[] bruh = classWrapper.getNode().name.split("[./]");
                        classWrapper.getNode().sourceFile = bruh[bruh.length - 1] + ".java";

                        boolean folderClassesEnabled = Ambien.get.transformerManager.getTransformer("folder-classes").isEnabled();

                        boolean duplicateResourcesEnabled = Ambien.get.transformerManager.getTransformer("duplicate-resources").isEnabled();
                        boolean dupClassesEnabled = DuplicateResources.dupClasses.isEnabled();

                         {
                            if (duplicateResourcesEnabled && dupClassesEnabled) {
                                for (int x = 1; x <= DuplicateResources.dupAmount.getValue(); x++) {
                                    IOUtil.writeEntry(stream, name + StringRepeat.repeat("\u0000", x), IOUtil.duplicateData(classWrapper.toByteArray()));
                                }
                            }
                            if (folderClassesEnabled && FolderClasses.folderClasses.isEnabled()) {
                                // Might break your jar
                                // :D
                                IOUtil.writeEntry(stream, name + "/", classWrapper.toByteArray());

                            } else
                                IOUtil.writeEntry(stream, name, classWrapper.toByteArray());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });


        if (Ambien.get.transformerManager.getTransformer("crasher").isEnabled() && Crasher.shitClasses.isEnabled()) {
            // Might break your jar
            for (int i = 0; i < Crasher.shitAmount.getValue(); i++) {
                Crasher.addShitClasses(null, stream);
                Crasher.addShitClasses("META-INF/", stream);
            }
        }


        // Set zip comment
        try {
            if (
                    Ambien.get.transformerManager.getTransformer("metadata").isEnabled()
                            &&
                            !Metadata.commentText.getValue().isEmpty()
            ) {
                stream.setComment(Metadata.commentText.getValue());
            }
        } catch (Exception e) {
            Ambien.logger.error("Failed to set zip comment due to  " + e.getMessage());
        }
        stream.flush();
        // 如果不关这个stream那么jar会爆炸
        stream.close();
        System.out.println(RemapTransformer.mapping);
        return oj;
    }

    private void processBoom(JarOutputStream stream) throws IOException {
        // 会炸 别用
        
        if(
                Ambien.get.transformerManager.getTransformer("string-encryption").isEnabled()
                &&
                        StringEncryptionManager.boom.isEnabled()
                &&      StringEncryptionManager.allatoriStringEncryption.isEnabled()
        ){
            ClassNode node = new ClassNode(ASM9);


            node.name = boomNodeName;
            node.superName = "java/lang/Object";
            node.access = ACC_PUBLIC;
            node.version = V1_8;

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            //<editor-fold desc="Write shit">
            // 定义方法访问标志和签名
            // 假设 'methodName' 和 'descriptor' 是预先定义的常量或字段
            MethodBuilder methodBuilder = MethodBuilder.builder()
                    .name(boomerName)
                    .access(ACC_PUBLIC | ACC_STATIC)
                    .desc("(Ljava/lang/String;I)Ljava/lang/String;")
                    .build();
            // 如果 GOTOASMUtils 需要，应用 synthetic 和 bridge 标志
            if (GOTOASMUtils.shouldMarkAsSynthetic(methodBuilder.buildNode())) {
                methodBuilder.addAccess(ACC_SYNTHETIC);
            }
            if (GOTOASMUtils.shouldMarkAsBridge(methodBuilder.buildNode())) {
                methodBuilder.addAccess(ACC_BRIDGE);
            }

            MethodNode mv = methodBuilder.buildNode();
            {
                // --- 方法代码生成开始 ---
                mv.visitCode();

                // 假设 mv 是已有的 MethodVisitor
                Label defaultLabel = new Label();
                Label endSwitch = new Label();
                final int cases = 255;
                Label[] labels = new Label[cases];
                for (int i = 0; i < cases; i++) {
                    labels[i] = new Label();
                }

                // 1. 加载第二个 int 实参
                mv.visitVarInsn(ILOAD, 1);

                // 2. 压入 255
                mv.visitIntInsn(SIPUSH, 255);

                // 3. 按位与
                mv.visitInsn(IAND);

                // 4. 创建 table switch
                mv.visitTableSwitchInsn(0, 254, defaultLabel, labels);

                // 5. 生成每个 case 分支
                for (int i = 0; i < cases; i++) {
                    mv.visitLabel(labels[i]);
                    // 给第二个实参赋一个随机值
                    int randomVal = keyStore.getOrDefault(
                            Integer.valueOf(i),
                            // Confuse
                            ThreadLocalRandomManager.theThreadLocalRandom.nextInt(min,max)
                    );
                    mv.visitIntInsn(SIPUSH, randomVal);
                    mv.visitVarInsn(ISTORE, 1);
                    mv.visitJumpInsn(GOTO, endSwitch);
                }

                // 6. default 分支
                mv.visitLabel(defaultLabel);
                {
                    // Default was 255, due to & 255
                    int randomVal = keyStore.getOrDefault(
                            Integer.valueOf(255),
                            // Confuse
                            ThreadLocalRandomManager.theThreadLocalRandom.nextInt(min,max)
                    );
                    mv.visitIntInsn(SIPUSH, randomVal);
                    mv.visitVarInsn(ISTORE, 1);
                    mv.visitJumpInsn(GOTO, endSwitch);
                }
                mv.visitJumpInsn(GOTO, endSwitch);

                // 7. 结束标签
                mv.visitLabel(endSwitch);

                // new java/lang/RuntimeException
                mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
                // dup
                mv.visitInsn(DUP);
                // invokespecial java/lang/RuntimeException.<init>:()V
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false);
                // astore_2 (rte)
                mv.visitVarInsn(ASTORE, 2);

                // aload_2
                mv.visitVarInsn(ALOAD, 2);
                // invokevirtual java/lang/RuntimeException.getStackTrace:()[Ljava/lang/StackTraceElement;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/RuntimeException", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
                // iconst_1
                mv.visitInsn(ICONST_1);
                // aaload
                mv.visitInsn(AALOAD);
                // astore_3 (stackTraceElement)
                mv.visitVarInsn(ASTORE, 3);

                // aload_0 (s)
                mv.visitVarInsn(ALOAD, 0);
                // invokevirtual java/lang/String.toCharArray:()[C
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
                // astore 4 (in)
                mv.visitVarInsn(ASTORE, 4);

                // ldc "#"
                mv.visitLdcInsn(combiner);
                // astore 5 (string)
                mv.visitVarInsn(ASTORE, 5);

                // new java/lang/StringBuilder
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                // dup
                mv.visitInsn(DUP);
                // invokespecial java/lang/StringBuilder.<init>:()V
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                // aload_3
                mv.visitVarInsn(ALOAD, 3);
                // invokevirtual java/lang/StackTraceElement.getClassName:()Ljava/lang/String;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
                // invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                // aload 5
                mv.visitVarInsn(ALOAD, 5);
                // invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                // aload_3
                mv.visitVarInsn(ALOAD, 3);
                // invokevirtual java/lang/StackTraceElement.getMethodName:()Ljava/lang/String;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
                // invokevirtual java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                // invokevirtual java/lang/StringBuilder.toString:()Ljava/lang/String;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                // invokevirtual java/lang/String.toCharArray:()[C
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
                // astore 6 (k)
                mv.visitVarInsn(ASTORE, 6);

                // aload 4
                mv.visitVarInsn(ALOAD, 4);
                // arraylength
                mv.visitInsn(ARRAYLENGTH);
                // newarray 10 (char)
                mv.visitIntInsn(NEWARRAY, T_CHAR);
                // astore 7 (out)
                mv.visitVarInsn(ASTORE, 7);

                // iconst_0
                mv.visitInsn(ICONST_0);
                // istore 8 (j)
                mv.visitVarInsn(ISTORE, 8);

                // For-loop structure
                Label loopCondition = new Label();
                mv.visitLabel(loopCondition);
                // iload 8
                mv.visitVarInsn(ILOAD, 8);
                // aload 4
                mv.visitVarInsn(ALOAD, 4);
                // arraylength
                mv.visitInsn(ARRAYLENGTH);
                Label loopEnd = new Label();
                // if_icmpge L3
                mv.visitJumpInsn(IF_ICMPGE, loopEnd);

                Label loopBody = new Label();
                mv.visitLabel(loopBody);
                // aload 7 (out)
                mv.visitVarInsn(ALOAD, 7);
                // iload 8 (j)
                mv.visitVarInsn(ILOAD, 8);
                // aload 4 (in)
                mv.visitVarInsn(ALOAD, 4);
                // iload 8 (j)
                mv.visitVarInsn(ILOAD, 8);
                // caload
                mv.visitInsn(CALOAD);
                // aload 6 (k)
                mv.visitVarInsn(ALOAD, 6);
                // iload 8 (j)
                mv.visitVarInsn(ILOAD, 8);
                // aload 6 (k)
                mv.visitVarInsn(ALOAD, 6);
                // arraylength
                mv.visitInsn(ARRAYLENGTH);
                // irem
                mv.visitInsn(IREM);
                // caload
                mv.visitInsn(CALOAD);
                // ixor
                mv.visitInsn(IXOR);
                // iload_1 (i)
                mv.visitVarInsn(ILOAD, 1);
                // ixor
                mv.visitInsn(IXOR);
                // i2c
                mv.visitInsn(I2C);
                // castore
                mv.visitInsn(CASTORE);

                // iinc 8 1 (j++)
                mv.visitIincInsn(8, 1);
                // goto L2
                mv.visitJumpInsn(GOTO, loopCondition);

                mv.visitLabel(loopEnd);
                // new java/lang/String
                mv.visitTypeInsn(NEW, "java/lang/String");
                // dup
                mv.visitInsn(DUP);
                // aload 7
                mv.visitVarInsn(ALOAD, 7);
                // invokespecial java/lang/String.<init>:([C)V
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "intern", "()Ljava/lang/String;", false);
                // areturn
                mv.visitInsn(ARETURN);

                Label endLabel = new Label();
                mv.visitLabel(endLabel);

                // 设置操作数栈和局部变量表的最大大小。
                // 使用 ClassWriter.COMPUTE_MAXS 会自动计算，更方便。
                mv.visitMaxs(0, 0); // 让 ASM 自动计算最大栈和局部变量数
                mv.visitEnd();
                // 将新创建的方法添加到类节点中

            }
            node.methods.add(mv);
            //</editor-fold>

            node.accept(writer);

            IOUtil.writeEntry(stream, boomNodeName + ".class",writer.toByteArray());
        }
    }
}
