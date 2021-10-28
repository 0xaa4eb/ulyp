package com.ulyp.core.util;

import com.ulyp.core.Method;

/**
 * User provided matcher for the method where recording should start.
 * A user should specify it in the following format as string: <simple class name>.<method name>
 * Examples:
 *  Runnable.run
 *  SpringApplication.main
 *
 * Wildcards are also supported:
 *  Runnable.*
 *  *.*
 */
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

    public boolean matches(Method methodRepresentation) {
        return (isMethodWildcard || methodRepresentation.getName().equals(methodName)) && classMatcher.matches(methodRepresentation.getDeclaringType());
    }

    @Override
    public String toString() {
        return classMatcher + "." + methodName;
    }
}
