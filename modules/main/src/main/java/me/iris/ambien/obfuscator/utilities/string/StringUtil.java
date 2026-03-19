package me.iris.ambien.obfuscator.utilities.string;

import cn.a114.commonutil.random.ThreadLocalRandomManager;
import lombok.experimental.UtilityClass;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.utilities.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// shut up!
@UtilityClass
public class StringUtil {

    private final boolean badWords = false;

    private static final Set<String> usedNames = new HashSet<>();
    private static final char[] CHARS = "$_0123456789abcdefghijlkmnopqrstuvwxyzABCDEFGHIJLKMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] RANDOM_CHARS = "abcdefghijlkmnopqrstuvwxyzABCDEFGHIJLKMNOPQRSTUVWXYZ".toCharArray();


    // 鿃鿶鿩鿣龻鿷鿏龺鿸鿅鿗龼鿪鿫鿎鿠龱鿂鿢龵鿓龿龫鿲鿹鿾龴
    // What?!
    private static final String[] xD = "鿃,鿶,鿩,鿣,龻,鿷,鿏,龺,鿸,鿅,鿗,龼,鿪,鿫,鿎,鿠,龱,鿂,鿢,龵,鿓,龿,龫,鿲,鿹,鿾,龴".split(",");

    private final List<String> ILLEGAL_JAVA_NAMES = Arrays.asList(
            "abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do",
            "double", "else", "enum", "extends",
            "false", "final", "finally", "float",
            "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface",
            "long", "native", "new", "null",
            "package", "private", "protected", "public",
            "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while"
    );


    /**
     * Generates a random string of the specified length using characters from the
     * provided pool.
     *
     * @param len  The length of the random string to generate.
     * @param isBad Uses a set of non-ASCII characters; otherwise, uses standard
     * @return A random string of the specified length.
     */
    @Deprecated
    public String randomStringIZ(final int len, final boolean isBad) {
        // Final mark removed because we need to gc this shit
        // Use current time in milliseconds as the seed to not repeat
        Random insurace = new Random(System.nanoTime());

        Random random = new Random(System.currentTimeMillis() + insurace.nextInt());

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            // To make this method human-readable, we did this.


            builder.append(
                    isBad ?
                            xD[random.nextInt(xD.length)]
                            :
                            RANDOM_CHARS[random.nextInt(RANDOM_CHARS.length)]
            );

        }

        return builder.toString();

    }

    public static String randomStringByNaming(final int len, Namings naming) {
        // Final mark removed because we need to gc this shit
        // Use current time in milliseconds as the seed to not repeat
        Random insurace = new Random(System.nanoTime());

        Random random = new Random(System.currentTimeMillis() + insurace.nextInt());

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            // To make this shit human-readable, we did this.

            switch (naming) {
                // a to Z
                case aZ:
                    builder.append(RANDOM_CHARS[random.nextInt(RANDOM_CHARS.length)]);
                    break;

                case iIl:
                    builder.append(
                            Namings.iIl.chars.split("")
                                    [ThreadLocalRandomManager.theThreadLocalRandom.nextInt(3000) % 3]
                    );
                    break;

                case nonAscii:
                    builder.append(xD[random.nextInt(xD.length)]);
                    break;
            }


        }

        return builder.toString();

    }

    public String genName(final int len) {


        // should keep

        // Check keep name
        // Do not rename shits marked as keep
        if (Ambien.get.naming.equals("keep")) {
            return StringUtil.getNewName(Ambien.get.naming, "");
        }

        String name;
        do {
            name = StringUtil.randomStringIZ(len, badWords);
        } while (usedNames.contains(name));

        usedNames.add(name);
        return name;
    }

    public String randomIllegalJavaName() {
        return ILLEGAL_JAVA_NAMES.get(ThreadLocalRandom.current().nextInt(0, ILLEGAL_JAVA_NAMES.size()));
    }


    public String randomSpace() {
        return randomStringIS(ThreadLocalRandom.current().nextInt(5, 10), "\n\u3000\u2007");
    }

    public static String randomStringByStringList(int length, List<String> stringPool) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            stringBuilder.append(stringPool.get(ThreadLocalRandom.current().nextInt(0, stringPool.size())));
        }

        return stringBuilder.toString();
    }

    public static String randomStringIS(int length, String pool) {
        StringBuilder stringBuilder = new StringBuilder();

        // Starts at 0 was a very buggy issue
        // So now it starts with 1
        for (int i = 1; i <= length; i++) {
            stringBuilder.append(
                    pool.charAt(
                            ThreadLocalRandom
                                    .current()
                                    .nextInt(0,
                                            (
                                                    pool.length()==0?1:pool.length()
                                            )
                                    )
                    )
            );
        }

        return stringBuilder.toString();
    }

    public static List<String> readDictionaryFromFile(String filePath) {
        List<String> dictionary = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public static @NotNull List<String[]> readCopyPastes (String filePath) throws IOException{
        List<String[]> copyPastes = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            List<String> currentAhegao = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (!currentAhegao.isEmpty()) {
                        copyPastes.add(currentAhegao.toArray(new String[0]));
                        currentAhegao.clear();
                    }
                } else {
                    currentAhegao.add(line);
                }
            }

            if (!currentAhegao.isEmpty()) {
                copyPastes.add(currentAhegao.toArray(new String[0]));
            }

            reader.close();

        return copyPastes;
    }



    /**
     * Generates a new name based on the provided mode and prefix.
     * If the mode is "barcode", it generates a random string using the iIl naming convention.
     * If the mode ends with ".txt", it reads names from the specified file and returns a random one with the prefix.
     * Otherwise, it generates a random string with the specified prefix.
     *
     * @param mode   The mode to determine how to generate the name.
     * @param prefix The prefix to prepend to the generated name.
     * @return A new unique name.
     */
    public static String getNewName(String mode, String prefix) {

        if (Arrays.equals(mode.getBytes(), "barcode".getBytes())) {
            return randomStringByNaming(16, Namings.iIl);
        }

        if (mode.endsWith(".txt")) {
            List<String> stringList = readDictionaryFromFile(mode);
            if (!stringList.isEmpty()) {
                String name;
                do {
                    int randomIndex = MathUtil.randomInt(0, stringList.size());
                    name = prefix + stringList.get(randomIndex);
                } while (usedNames.contains(name));
                usedNames.add(name);
                return name;
            }
        }

        return prefix + StringUtil.randomStringIZ(MathUtil.randomInt(10, 50), badWords);
    }

    public static String getNewName(String mode) {
        return getNewName(mode, "");
    }

    public String build(final String[] strs) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            builder.append(strs[i]);
            if (i != strs.length - 1)
                builder.append(' ');
        }

        return builder.toString();
    }


    public boolean containsNonAlphabeticalChars(final String str) {
        final List<char[]> charList = Collections.singletonList(CHARS);

        for (char c : str.toCharArray()) {
            if (charList.contains(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isEmpty(@Nullable String s){
        // java有短路机制，可以通过这个来防止NPE
        return s == null || s.isEmpty();
    }
}
