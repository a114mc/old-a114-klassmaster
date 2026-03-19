package me.iris.ambien.obfuscator.transformers.impl.optimization;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.exceptions.SettingConflictException;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.transformers.impl.exploits.Crasher;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import me.iris.ambien.obfuscator.utilities.kek.myj2c.Myj2cASMUtils;
import me.iris.ambien.obfuscator.utilities.string.Namings;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@TransformerInfo(
        name = "remove-debug-info",
        category = Category.OPTIMIZATION,
        stability = Stability.STABLE,
        ordinal = Ordinal.HIGH,
        description = "Removes information from classes related to debugging."
)
public class RemoveDebugInfo extends Transformer {
    private static final String[] CRASH_SIGNATURES = {"[B", "[I", "[Z", "[J"};
    private static final String JAVA_EXTENSION = ".java";
    public final BooleanSetting removeInnerClasses = new BooleanSetting(
            "remove-inner-classes", false);
    public final StringSetting sourceDebug = new StringSetting(
            "source-debug", "+a114 klassmaster");
    public final StringSetting sourceFile = new StringSetting("source-file", "keep");
    public final StringSetting signature = new StringSetting("signature", "keep");
    public final BooleanSetting removeVariables = new BooleanSetting(
            "remove-local-variables", true);

    public BooleanSetting ln_randomize = new BooleanSetting("randomize-line-numbers", false);
    public BooleanSetting ln_remove = new BooleanSetting("remove-line-numbers", true);

    @Override
    public void transform(JarWrapper wrapper) {
        if (ln_remove.isEnabled()) {
            if(ln_randomize.isEnabled()){
                Ambien.logger.warn("WTF? Randomize and remove line number? Currently we're removing...");
            }
            getClasses(wrapper).forEach(cw -> {
                cw.getTransformableMethods().forEach(
                        mw -> {
                            mw.getInstructions()
                                    .filter(LineNumberNode.class::isInstance)
                                    .map(LineNumberNode.class::cast)
                                    .forEach(insn -> {
                                        mw.getNode().instructions.remove(
                                                insn);
                                    });
                        }
                );
            });
        }
        else {
            if (ln_randomize.isEnabled()) {
                getClasses(wrapper).forEach(rcw -> {
                    rcw.getTransformableMethods()
                            .forEach(rmw -> {
                                rmw.getInstructions()
                                        .filter(LineNumberNode.class::isInstance)
                                        .map(LineNumberNode.class::cast)
                                        .forEach(ln -> {
                                            ln.line = MathUtil.randomInt()
                                                    & Character.MAX_VALUE;
                                        });
                            });
                });
            }
        }
        validateSettings();
        getClasses(wrapper).forEach(this::processClass);
    }

    private void validateSettings() {
        if (Ambien.get.transformerManager.getTransformer("crasher")
                .isEnabled()
                && Crasher.junkSignatures.isEnabled()) {
            throw new SettingConflictException(
                    "The remove-debug-info transformer can't be used while using the junk-signatures setting in the crasher transformer. (Disable one)");
        }
    }

    private void processClass(ClassWrapper classWrapper) {
        ClassNode node = classWrapper.getNode();

        processVarNames(node);
        processSourceDebug(node);
        processSourceFile(node);
        processSignature(node);

        if (removeInnerClasses.isEnabled()) {
            // Very stupid way to remove inner classes
            node.innerClasses = new ArrayList<>();
        }
    }

    private void processVarNames(ClassNode node) {
        if (removeVariables.isEnabled()) {
            node.methods
                    .forEach(methodNode -> {
                        if (methodNode.localVariables != null) {
                            methodNode.localVariables = new ArrayList<>();
                        }
                        Ambien.logger.debug(String.format(
                                "Removed local variable name(s) from %s:%s",
                                Myj2cASMUtils.getName(node), methodNode.name
                        ));
                    });
        }
//		else {
//			Ambien.logger.info("Ok cool shit");
//			node.methods.forEach(
//					mn -> {
//						if (mn.localVariables != null) {
//							for (int i = 0; i < mn.localVariables.size(); i++) {
//								LocalVariableNode sbASM = mn.localVariables.get(i);
//								sbASM.name = "PROTECTED BY ABFUSCATOR";
//								mn.localVariables.set(i, sbASM);
//							}
//
//						}
//					}
//			);
//		}
    }

    private void processSourceDebug(ClassNode node) {
        DebugMode mode = getDebugMode(sourceDebug.getValue());
        int length = node.sourceDebug == null ? 24 : node.sourceDebug.length();

        switch (mode) {
            case KEEP:
                break;
            case RANDOM:
                node.sourceDebug = StringUtil.randomStringByNaming(8, Namings.az);
                break;
            case BARCODE:
                node.sourceDebug = StringUtil.randomStringByNaming(64, Namings.iIl);
                break;
            case CUSTOM:
                //org.objectweb.asm.tree.ClassNode.sourceDebug
                node.sourceDebug = sourceDebug.getValue().startsWith("+")?sourceDebug.getValue().substring(1):StringUtil.randomStringIS(
                        length, sourceDebug.getValue());
                break;
        }
    }

    private void processSourceFile(ClassNode node) {
        if (node.sourceFile == null || node.sourceFile.isEmpty()) {
            node.sourceFile = node.name.replace('/', '.') + JAVA_EXTENSION;
        }

        DebugMode mode = getDebugMode(sourceFile.getValue());
        String value = sourceFile.getValue();

        switch (mode) {
            case KEEP:
                break;
            case RANDOM:
                node.sourceFile = StringUtil.randomStringByNaming(
                        8, Namings.az) + JAVA_EXTENSION;
                break;
            case BARCODE:
                node.sourceFile = StringUtil.randomStringByNaming(
                        node.sourceFile.length() - 5, Namings.iIl) + JAVA_EXTENSION;
                break;
            case CUSTOM:
            default:
                String node_name = node.name.replace('/', '.');
                String[] p = node_name.split("\\.");
                int sb = p.length;
                String trueName = p[sb - 1];
                // It just works
                node.sourceFile =
                        value.startsWith("+") ? value.substring(1) : value.equals("fix") ? trueName + JAVA_EXTENSION :
                                StringUtil.randomStringIS(
                                        sourceFile.getValue()
                                                .length(), sourceFile.getValue()
                                )
                ;
                break;
        }
    }

    private void processSignature(ClassNode node) {
        DebugMode mode = getDebugMode(signature.getValue());

        switch (mode) {
            case KEEP:
                break;
            case CRASH:
                node.signature = CRASH_SIGNATURES[ThreadLocalRandom.current()
                        .nextInt(CRASH_SIGNATURES.length)];
                break;
            case CUSTOM:
                node.signature = signature.getValue();
                break;
        }
    }

    private DebugMode getDebugMode(String value) {
        if (value == null) {
            return DebugMode.KEEP;
        }
        switch (value.toLowerCase()) {
            case "keep":
                return DebugMode.KEEP;
            case "random":
                return DebugMode.RANDOM;
            case "barcode":
                return DebugMode.BARCODE;
            case "crash":
                return DebugMode.CRASH;
            default:
                return DebugMode.CUSTOM;
        }
    }

    private enum DebugMode {
        KEEP,
        RANDOM,
        BARCODE,
        CRASH,
        CUSTOM
    }
}