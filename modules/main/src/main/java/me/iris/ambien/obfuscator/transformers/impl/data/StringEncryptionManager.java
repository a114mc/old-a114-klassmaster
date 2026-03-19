/*
 * Bruhfuscator-CN
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.transformers.impl.data;

import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringListSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.transformers.data.Category;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.transformers.data.annotation.TransformerInfo;
import me.iris.ambien.obfuscator.transformers.impl.data.string.*;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;

import java.util.ArrayList;

import static me.iris.ambien.obfuscator.transformers.impl.data.string.GotoStringEncryption.gotoEncryption;

@TransformerInfo(
        name = "string-encryption", category = Category.DATA,
        stability = Stability.STABLE,
        description = "Encrypts string using xor & random keys, DashO mode supports 1 2 4 7 10"
)
public class StringEncryptionManager extends Transformer {
    // TODO: Randomize descriptor argument order & add decoy args

    public static final StringListSetting stringBlacklist = new StringListSetting(
            "string-blacklist", new ArrayList<>());
    /**
     * List of string that won't be encrypted
     */
    public static final BooleanSetting colonialEncryption = new BooleanSetting(
            "colonial", false);
    public static final BooleanSetting gotoEncryption = new BooleanSetting("goto", false);

    public static final BooleanSetting inlineXor = new BooleanSetting("inline-xor", false);
    public static final BooleanSetting inlineAES = new BooleanSetting("inline-aes", false);
    public static final BooleanSetting souvenirEncryption = new BooleanSetting(
            "souvenir", false);
    public static final BooleanSetting allatoriStringEncryption = new BooleanSetting(
            "bruh", false);
    public static final BooleanSetting boom = new BooleanSetting(
            "bruh-boom", false);
    public static final BooleanSetting dashO = new BooleanSetting(
            "dash-O", false);
    // 1 2 4 7 10
    public static final NumberSetting<Integer> dashO_level = new NumberSetting<Integer>(
            "dash-O-level", (Integer) 1);
    public static final BooleanSetting dashO_name = new BooleanSetting(
            "dash-O-style-method-name-for-dash-O-methods", true);


    @Override
    public void transform(JarWrapper wrapper) {
        wrapper.getClasses().forEach(classWrapper -> {
            if(Ambien.exclusionManager.isClassExcluded("string-encryption", classWrapper.getNode())){
               return;
            }
            if (colonialEncryption.isEnabled()) {
                ColonialStringEncryption.colonialEncryption(classWrapper);
            }
            if (souvenirEncryption.isEnabled()) {
                SouvenirStringEncryption.souvenirEncryption(classWrapper);
            }
            if (gotoEncryption.isEnabled()) {
                gotoEncryption(classWrapper.getNode());
            }
            if (allatoriStringEncryption.isEnabled()) {
                AllatoriLikeStringEncryption.process(classWrapper.getNode());
            }
            if (inlineXor.isEnabled()) {
                InlineXorStringEncryption.doit(classWrapper.getNode());
            }
            if (inlineAES.isEnabled()) {
                InlineAESStringEncryption.doit(classWrapper.getNode());
            }

            if (dashO.isEnabled()) {
                DashOStringEncryption.process(classWrapper.getNode());
            }
        });
    }
}
