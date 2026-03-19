/*
 * Copyright (c) 2025. a114mc
 *
 * All rights reserved.
 */
package cn.a114.commonutil.j8;

public final class StringRepeat {

    private StringRepeat () {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    /**
     * Repeats the given string a specified number of times.
     *
     * @param str   the string to repeat
     * @param count the number of times to repeat the string
     * @return a new string that is the concatenation of the original string repeated count times
     */
    public static String repeat (String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
