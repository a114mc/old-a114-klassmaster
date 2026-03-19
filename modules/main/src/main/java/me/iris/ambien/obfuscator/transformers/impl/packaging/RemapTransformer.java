package me.iris.ambien.obfuscator.transformers.impl.packaging;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.data.implementations.StringListSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import me.iris.ambien.obfuscator.utilities.string.Namings;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@TransformerInfo(
        name = RemapTransformer.name,
        category = Category.PACKAGING,
        description = "Advanced remapper(without field method support, only for classes)",
        stability = Stability.STABLE,
        // before invokedynamic
        ordinal = Ordinal.HIGH
)
public class RemapTransformer extends Transformer {

    public static final String name = "remapper";

    private static boolean prefixProcessed = false;

    public StringSetting prefix = new StringSetting("flatten-prefix","loftily/");
    public StringListSetting suffixes = new StringListSetting("flatten-suffix",shit);

    private static final ArrayList<String> shit = new ArrayList<>();

    static {
        try {
            String[] temp = {
                    "你个没速度的小废物",
                    "你为什么在这里耀武扬威的",
                    "我现在在这埋汰你呢小老弟",
                    "我好象你爸爸似的你难道自己不清楚现在的情况吗",
                    "然后你完全没有力量你明白你的扣字垃圾吗小废物"};
            shit.addAll(Arrays.asList(temp));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 完整的映射表，由旧名称到新名称
    public static final Map<String, String> mapping = new HashMap<>();

    // Use a Set for faster contains() checks
    private static final Set<String> usedNames = new HashSet<>();

    private String generateNewName() {
        int length = 2;
        // defensive limit to avoid infinite loop (adjust as you wish)
        final int MAX_LENGTH = 256;

        String name = prefix.getValue();
        while (length <= MAX_LENGTH) {
            name += StringUtil.randomStringByNaming(length, Namings.aZ);
            // check the actual generated name (not the list itself)
            if (!usedNames.contains(name)) {
                usedNames.add(name);
                name += suffixes.getOptions().get(ThreadLocalRandom.current().nextInt(0,suffixes.getOptions().size()));
                return name;
            }
            ++length; // try longer name to reduce collisions
        }

        // If we reach here, something is weird — fail loudly rather than produce duplicates
        throw new IllegalStateException("Failed to generate a unique name after lengths up to " + MAX_LENGTH);
    }

    @Override
    public void transform(JarWrapper wrapper) {
        processPrefix();
        // 第一步：构建完整的旧名称 -> 新名称映射表
        buildMappingTable(wrapper);

        // 第二步：使用自定义的 Remapper 应用映射
        applyRemapping(wrapper);
    }

    private void processPrefix(){
        if(!prefixProcessed) {
            String v = prefix.getValue();
            v = v.replace(".", "/");
            prefix.setValue(v);
        }
        prefixProcessed = true;
    }

    private void buildMappingTable(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            if (classWrapper.isLibraryClass()){
                return;
            }
            ClassNode cn = classWrapper.getNode();
            if(!Ambien.exclusionManager.isClassExcluded(RemapTransformer.name, cn)){
                // 为类名生成映射
                String newClassName = generateNewName();
                mapping.put(cn.name, newClassName);
            }

//            // 为方法名生成映射
//            cn.methods.forEach(methodNode -> {
//                // 如果方法在黑名单中，则跳过
//                if (isBlacklisted(cn, methodNode)) {
//                    return;
//                }
//                // 使用完整签名作为键："类名.方法名方法描述符"
//                String signature = originalClassName + "." + methodNode.name + methodNode.desc;
//                mapping.put(signature, generateNewName());
//            });
//
//            // 为字段名生成映射
//            cn.fields.forEach(fieldNode -> {
//                String signature = originalClassName + "." + fieldNode.name + ":" + fieldNode.desc;
//                mapping.put(signature, generateNewName());
//            });
        });
    }


    private void applyRemapping(JarWrapper wrapper) {
        // 创建我们自己的 Remapper 类，它将使用我们构建的映射表
        Remapper customRemapper = new RemapperLogic(mapping);

        // Do not change!
        // For remapping purposes
        wrapper.getClasses().forEach(classWrapper -> {
            // 获取 ClassWrapper 内部的 ClassNode
            ClassNode originalNode = classWrapper.getNode();

            if(!Ambien.exclusionManager.isClassExcluded(RemapTransformer.name, originalNode)){
                // 创建一个新的 ClassNode 来保存重映射后的结果
                ClassNode remappedNode = new ClassNode();

                // 使用 ClassRemapper 将 originalNode 的内容重映射到 remappedNode
                ClassRemapper classRemapper = new ClassRemapper(remappedNode, customRemapper);
                originalNode.accept(classRemapper);

                // 将重映射后的 ClassNode 更新回 ClassWrapper
                // 假设 ClassWrapper 有一个类似于 setNode(ClassNode) 的方法
                classWrapper.setNode(remappedNode);
            }
        });
    }

    // 实现了自定义重命名逻辑的 Remapper
    private class RemapperLogic extends Remapper {
        private final Map<String, String> mapping;

        public RemapperLogic(Map<String, String> mapping) {
            this.mapping = mapping;
        }

        @Override
        public String map(String key) {
            // 这个方法用于重命名类名
            return mapping.getOrDefault(key, key);
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            // 这个方法用于重命名方法名
            String signature = owner + "." + name + descriptor;
            return mapping.getOrDefault(signature, name);
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            // 这个方法用于重命名字段名
            String signature = owner + "." + name + ":" + descriptor;
            return mapping.getOrDefault(signature, name);
        }
    }

    /**
     * 判断一个方法是否在黑名单中，不应该被重命名。
     * 这部分逻辑来自你提供的代码，并稍作调整以适应当前框架。
     * 注意：由于无法访问外部库的 HierarchyUtils，相关检查被注释掉了。
     */
    private boolean isBlacklisted(ClassNode classNode, MethodNode method) {
        if(GOTOASMUtils.isSpecialMethod(method)
                || GOTOASMUtils.isMainMethod(method)
                || GOTOASMUtils.isMethodNodeInitializerOrConstructor(method)
        ){
            return true;
        }

        // 枚举类型特殊方法
        if ((classNode.access & ACC_ENUM) != 0) {
            if (method.name.equals("values") || method.name.equals("valueOf")) {
                return true;
            }
        }

        // 如果是注解类型
        if ((classNode.access & ACC_ANNOTATION) != 0) {
            return true;
        }

        // TODO: 完善父类和接口的黑名单逻辑
        // if (HierarchyUtils.isMethodFromLibrary(classNode.name, method.name, method.desc)) return true;
        // if (HierarchyUtils.hasAnnotation(classNode, "java/lang/FunctionalInterface")) return true;

        return false;
    }
}
