package com.ulyp.core.util;

public class ClassUtils {

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
