package cn.a114.klassmaster.gui.util;

import java.io.InputStream;
import java.net.URL;

public final class ResourceUtil {
    public static URL res(String name){
        return ResourceUtil.class.getResource(name);
    }
    public static URL res(Class<?> cl, String name){
        return cl.getResource(name);
    }
    public static InputStream streamRes(String name){
        return ResourceUtil.class.getResourceAsStream(name);
    }
    public static InputStream streamRes(Class<?> cl, String name){
        return cl.getResourceAsStream(name);
    }
}
