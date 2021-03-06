package com.ulyp.core.util;

import com.ulyp.core.MethodInfo;

public class MethodMatcher {

    private static final char SEPARATOR = '.';
    private static final String WILDCARD = "*";

    private final ClassMatcher classMatcher;
    private final String methodName;
    private final boolean isMethodWildcard;

    public static MethodMatcher parse(String text) {
        int separatorPos = text.lastIndexOf(SEPARATOR);
        // TODO regexp?
        if (separatorPos < 0) {
            throw new SettingsException("");
        }

        return new MethodMatcher(ClassMatcher.parse(text.substring(0, separatorPos)), text.substring(separatorPos + 1));
    }

    public MethodMatcher(Class<?> clazz, String methodName) {
        this(ClassMatcher.parse(clazz.getSimpleName()), methodName);
    }

    public MethodMatcher(ClassMatcher classMatcher, String methodName) {
        this.classMatcher = classMatcher;
        this.methodName = methodName;
        this.isMethodWildcard = methodName.equals(WILDCARD);
    }

    public boolean matches(MethodInfo methodRepresentation) {
        return (isMethodWildcard || methodRepresentation.getMethodName().equals(methodName)) && classMatcher.matches(methodRepresentation.getDeclaringType());
    }

    @Override
    public String toString() {
        return classMatcher + "." + methodName;
    }
}
