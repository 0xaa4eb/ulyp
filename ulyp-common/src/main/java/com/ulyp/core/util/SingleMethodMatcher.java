package com.ulyp.core.util;

import com.ulyp.core.Method;

/**
 * User provided matcher for the method where recording should start.
 * A user should specify it in the following format as string: <class name ant pattern>.<method name>
 * Examples:
 * **.Runnable.run
 * org.*.SpringApplication.main
 * com.test.**.run
 * com.test.TestClass.*
 * <p>
 * If recording should start at any method, then *.* string should be used
 */
public class SingleMethodMatcher implements MethodMatcher {

    private final TypeMatcher typeMatcher;
    private final String methodName;
    private final boolean isMethodWildcard;

    public SingleMethodMatcher(Class<?> clazz, String methodName) {
        this(TypeMatcher.parse(clazz.getName()), methodName);
    }

    public SingleMethodMatcher(TypeMatcher typeMatcher, String methodName) {
        this.typeMatcher = typeMatcher;
        this.methodName = methodName;
        this.isMethodWildcard = methodName.equals(WILDCARD);
    }

    public static SingleMethodMatcher parse(String text) {
        int separatorPos = text.lastIndexOf(SEPARATOR);
        if (separatorPos < 0) {
            throw new SettingsException("Invalid method matcher: " + text +
                    ". It should look something like this: **.Runnable.run");
        }

        return new SingleMethodMatcher(TypeMatcher.parse(text.substring(0, separatorPos)), text.substring(separatorPos + 1));
    }

    @Override
    public boolean matches(Method method) {
        return (isMethodWildcard || method.getName().equals(methodName)) && typeMatcher.matches(method.getDeclaringType());
    }

    @Override
    public String toString() {
        return typeMatcher + "." + methodName;
    }
}
