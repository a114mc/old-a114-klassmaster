package me.iris.ambien.obfuscator.utilities.string;

/**
 * Enum representing different naming conventions.
 * Each enum constant has a string associated with it that represents the characters used in that naming convention.
 */
public enum Namings {

    iIl("iIl"), az("abcdefghijlkmnopqrstuvwxyzABCDEFGHIJLKMNOPQRSTUVWXYZ"), aZ("aZ"), nonAscii("好");
    public final String chars;
    public static final String nonAsciiString = "好";

    private Namings(String chars) {
        this.chars = chars;
    }

    // Find enum by name
    public static Namings findByName(String name) throws IllegalArgumentException {
        // For each enum constant, check if the name matches (case-insensitive)
        for (Namings naming : values()) {
            if (naming.name().equalsIgnoreCase(name)) {
                return naming;
            }
        }
        // If no enum constant matches, throw an exception
        throw new IllegalArgumentException("No enum constant " + Namings.class.getCanonicalName() + "." + name);
    }
}
