package me.iris.ambien.obfuscator.transformers.impl.packaging;

import cn.a114.commonutil.random.ThreadLocalRandomManager;
import me.iris.ambien.obfuscator.builders.ClassBuilder;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringListSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.utilities.GOTOASMUtils;
import me.iris.ambien.obfuscator.utilities.IOUtil;
import me.iris.ambien.obfuscator.wrappers.ClassWrapper;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.jar.JarOutputStream;

/**
 * Adds a fake jar before the real jar.
 * Most RE tools don't read backwards like the JVM, so they will read the fake jar
 */
@TransformerInfo(
        name = "red-herring",
        category = Category.PACKAGING,
        ordinal = Ordinal.LOW,
        stability = Stability.STABLE,
        description = "Adds a fake jar before the real jar"
)
public class RedHerring extends Transformer {

	//<editor-fold desc="Swearing">
	/**
     * Adds junk data instead of a class file, this will result in a smaller jar when using this transformer
     */
    private final List<String> messagesList = Arrays.asList(
            "своего безглазого парнокопытного деда декомпиль, бездарность | you_need_to_train_more",
            // GitHub copilot generated English Translation
            "decompile your eyeless ungulate grandfather, talentless | you_need_to_train_more",
            "反编译你无眼的偶蹄动物祖父，L | you_need_to_train_more",

            // 58.48.129.112 meme
            "わたし..... 生きてる? | 王靖武"
    );
	//</editor-fold>

    public static BooleanSetting corrupt = new BooleanSetting("corrupt", false);
    public static StringSetting className = new StringSetting("class-name", "Main");

    public final StringListSetting watermark = new StringListSetting("text", messagesList);

    @Override
    public void transform(JarWrapper wrapper) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (corrupt.isEnabled()) {

            // https://www.loc.gov/preservation/digital/formats/fdd/fdd000354.shtml
            // https://en.wikipedia.org/wiki/Zip_%28file_format%29#Structure

            stream.write(0x50);
            stream.write(0x4B);
            stream.write(0x03);
            stream.write(0x04);
            final Random rand = new Random();
            final byte[] bytes = new byte[ThreadLocalRandomManager.theThreadLocalRandom.nextInt(1, 25)];
            rand.nextBytes(bytes);
            try {
                stream.write(bytes);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                throw new RuntimeException("Failed to write red herring byte!");
            }
        } else {
            String name = className.getValue();

            final ClassBuilder classBuilder = new ClassBuilder()
                    .setName(name)
                    .setSuperName(name)
                    .setAccess(ACC_PUBLIC)
                    .setVersion(V1_8);
            final ClassNode classNode = classBuilder.buildNode();

            // If empty, use the built-in dictionary
            if (!watermark.getOptions().isEmpty()) {

                List<String> inputList = watermark.getOptions();
                String[][] messages = new String[inputList.size()][2];

                for (int i = 0; i < inputList.size(); i++) {
                    String[] parts = inputList.get(i).split("\\s*\\|\\s*", -1);
                    // If the input is not in the correct format, throw an exception
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Red Herring: Invalid message format: " + inputList.get(i) + ". Expected format: 'message | key'");
                    }
                    messages[i][0] = parts[0].trim(); // e.g. "своего безглазого парнокопытного деда декомпиль, бездарность"
                    messages[i][1] = parts[1].trim(); // e.g. "you_need_to_train_more"
                }

                for (String[] messageData : messages) {
                    FieldNode fieldNode = new FieldNode(ACC_STATIC, messageData[1], "Ljava/lang/String;", null, null);
                    classNode.fields.add(fieldNode);
                }

                MethodNode clinit = GOTOASMUtils.getClinitMethodNodeOrCreateNew(classNode);
                InsnList clinitInstructions = clinit.instructions;

                for (String[] messageData : messages) {
                    clinitInstructions.add(new LdcInsnNode(messageData[0]));
                    clinitInstructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, name, messageData[1], "Ljava/lang/String;"));
                }

                clinitInstructions.add(new InsnNode(Opcodes.RETURN));
                classNode.methods.add(clinit);
            }

            final ByteArrayOutputStream jarBufferStream = new ByteArrayOutputStream();

            try (JarOutputStream jarOutputStream = new JarOutputStream(jarBufferStream)) {
                final ClassWrapper classWrapper = new ClassWrapper(name + ".class", classNode, false);
                // ?!
                IOUtil.writeEntry(jarOutputStream, name + ".class", classWrapper.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                stream.write(jarBufferStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Add red herring stream to jar wrapper streams
        wrapper.getOutputStreams().add(stream);
    }
}
