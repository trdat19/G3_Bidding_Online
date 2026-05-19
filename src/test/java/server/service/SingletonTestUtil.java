package server.service;

import java.lang.reflect.Field;

public class SingletonTestUtil {

    public static void resetSingleton(Class<?> clazz) throws Exception {
        Field instanceField = clazz.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}
