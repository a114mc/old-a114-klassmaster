/*
 * Copyright (c) 2025. a114mc
 *
 * All rights reserved.
 */
package cn.a114.commonutil.random;

import java.util.concurrent.ThreadLocalRandom;

// Why?!
public class ThreadLocalRandomManager {
    public static ThreadLocalRandom theThreadLocalRandom = ThreadLocalRandom.current();
}
