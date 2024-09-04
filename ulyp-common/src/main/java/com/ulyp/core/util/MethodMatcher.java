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
public interface MethodMatcher {

    char MATCHER_SEPARATOR = ',';
    char SEPARATOR = '.';
    String WILDCARD = "*";

    static MethodMatcher parse(String text) {
        if (text.indexOf(MATCHER_SEPARATOR) >= 0) {
            return CompoundMethodMatcher.parse(text);
        } else {
            return SingleMethodMatcher.parse(text);
        }
    }

    boolean matches(Method method);
}
