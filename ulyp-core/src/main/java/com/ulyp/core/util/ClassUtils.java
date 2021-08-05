package com.ulyp.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassUtils {

    private static final ConcurrentMap<Class<?>, String> clazzToSimpleName = new ConcurrentHashMap<>(20000);

    public static String getSimpleName(Class<?> clazz) {
        return clazzToSimpleName.computeIfAbsent(clazz, ClassUtils::getSimpleNameSafe);
    }

    public static String getSimpleNameSafe(Class<?> clazz) {
        try {
            return clazz.getSimpleName();
        } catch (Exception e) {
            return "???";
        }
    }

    public static String getSimpleNameFromName(String name) {
        int sepPos = -1;
        for (int i = name.length() - 1; i >= 0; i--) {
            char c = name.charAt(i);
            if (c == '.' || c == '$') {
                sepPos = i;
                break;
            }
        }

        if (sepPos > 0) {
            return name.substring(sepPos + 1);
        } else {
            return name;
        }
    }
}
