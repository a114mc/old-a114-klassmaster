package me.iris.ambien.obfuscator.transformers.data;

public enum Stability {
    /**
     * STABLE
     * <p><strong>Safe for use</strong></p>
     */
    STABLE("Stable"),

    /**
     * EXPERIMENTAL
     * <p><strong>Crash ALERT</strong></p>
     * */
    EXPERIMENTAL("Experimental");

    private final String name;

    Stability(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
