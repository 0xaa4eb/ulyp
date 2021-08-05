package com.ulyp.ui.util;

public class ClassNameUtils {

    public static String toSimpleName(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(lastDot + 1);
        } else {
            return name;
        }
    }
}
