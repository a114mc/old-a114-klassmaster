package me.iris.ambien.obfuscator.utilities.kek;

import java.util.ArrayList;
import java.util.List;

public class UnicodeDictionary extends ListDictionary {

    public static int CHINESE_BEGIN = 0x4E00;
    public static int CHINESE_END = 0x9FFF;
    private static final List<String> unicode = new ArrayList<>();
    public static final List<String> chinese = new ArrayList<>();


    static {
        for (int i = CHINESE_BEGIN; i <= CHINESE_END; i++) { // Chinese
            chinese.add(Character.toString((char) i));
        }

        unicode.addAll(chinese);
    }

    public UnicodeDictionary(int repeatTime) {
        super(repeatTime);
    }

    @Override
    public List<String> getList() {
        return unicode;
    }
}

