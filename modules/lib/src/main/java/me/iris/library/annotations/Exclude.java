package me.iris.library.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Obfuscator whitelist
 * <p></p>
 * @see me.iris.ambien.obfuscator.transformers.ExclusionManager#EXCLUDE_DESC
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
@SuppressWarnings("unused")
public @interface Exclude {
    // Empty
}
