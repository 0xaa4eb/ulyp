package com.ulyp.core.util;

import com.ulyp.core.Method;

/**
 * User provided matcher for the method where recording should start.
 * A user should specify it in the following format as string: <class name ant pattern>.<method name>
 * Examples:
 *  **.Runnable.run
 *  org.*.SpringApplication.main
 *  com.test.**.run
 *  com.test.TestClass.*
 *
 * If recording should start at any method, then *.* string should be used
 */
public class MethodMatcher {

    private static final char SEPARATOR = '.';
    private static final String WILDCARD = "*";

    private final ClassMatcher classMatcher;
    private final String methodName;
    private final boolean isMethodWildcard;

    public static MethodMatcher parse(String text) {
        int separatorPos = text.lastIndexOf(SEPARATOR);
        if (separatorPos < 0) {
            throw new SettingsException("Invalid method matcher: " + text +
                    ". It should look something like this: **.Runnable.run");
        }

        return new MethodMatcher(ClassMatcher.parse(text.substring(0, separatorPos)), text.substring(separatorPos + 1));
    }

    public MethodMatcher(Class<?> clazz, String methodName) {
        this(ClassMatcher.parse(clazz.getName()), methodName);
    }

    public MethodMatcher(ClassMatcher classMatcher, String methodName) {
        this.classMatcher = classMatcher;
        this.methodName = methodName;
        this.isMethodWildcard = methodName.equals(WILDCARD);
    }

    public boolean matches(Method method) {
        return  (isMethodWildcard || method.getName().equals(methodName)) && classMatcher.matches(method.getDeclaringType());
    }

    @Override
    public String toString() {
        return classMatcher + "." + methodName;
    }
}
