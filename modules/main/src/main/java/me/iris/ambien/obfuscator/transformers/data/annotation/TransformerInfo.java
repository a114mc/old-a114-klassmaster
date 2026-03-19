package me.iris.ambien.obfuscator.transformers.data.annotation;

import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Ordinal;
import me.iris.ambien.obfuscator.transformers.data.Stability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TransformerInfo {

    /**
     * Transformer name
     */
    String name();

    /**
     * Transformer category
     */
    Category category();

    /**
     * Stability.
     * In other words: <strong>will it destroy your program</strong>
     */
    Stability stability();


    /**
     * Priority
     * @see Ordinal
     */
    Ordinal ordinal() default Ordinal.STANDARD;

    /**
     * Transformer description
     */
    String description() default "No description provided.";

    boolean enabledByDefault() default false;
}
