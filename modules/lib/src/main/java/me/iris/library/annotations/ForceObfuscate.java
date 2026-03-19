package me.iris.library.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark classes/methods as force obfuscate, it ignores the exclusion configurations,
 *  but it DOESN'T forcefully enable the specified transformer.
 * @author a114
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
@SuppressWarnings("unused")
public @interface ForceObfuscate {
    /**
     * This is the transformer name, use ALL(ignores case) for all transformers
     */
    String[] value() default {"ALL"};
}
