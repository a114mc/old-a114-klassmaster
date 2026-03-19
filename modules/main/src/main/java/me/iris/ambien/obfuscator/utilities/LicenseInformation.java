/*
 * Bruhfuscator-CN
 * Copyright (c) 2025 a114mc, All Rights Reserved.
 */

package me.iris.ambien.obfuscator.utilities;

import java.util.HashMap;

public class LicenseInformation {
    private static final String AL2 = "Apache License, Version 2.0";
    private static final String MIT = "The MIT License";
    private static final String BSD3C = "BSD-3-Clause";
    public static HashMap<String, String> software_pair = new HashMap<>();
    static {
        software_pair.put("com.beust#jcommander",AL2);
        software_pair.put("org.projectlombok#lombok",MIT);
        software_pair.put("org.ow2.asm#asm",BSD3C);
        software_pair.put("org.ow2.asm#asm-commons",BSD3C);
        software_pair.put("org.ow2.asm#asm-tree",BSD3C);
        software_pair.put("org.ow2.asm#asm-analysis",BSD3C);
        software_pair.put("org.ow2.asm#asm-util",BSD3C);
        software_pair.put("com.google.guava#guava",AL2);
        software_pair.put("org.slf4j#slf4j-api",MIT);
        software_pair.put("org.slf4j#slf4j-simple",MIT);
        software_pair.put("org.jetbrains#annotations",AL2);
    }
}
