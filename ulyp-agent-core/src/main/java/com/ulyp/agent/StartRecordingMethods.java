package com.ulyp.agent;

import com.ulyp.core.Method;
import com.ulyp.core.util.CommaSeparatedList;
import com.ulyp.core.util.MethodMatcher;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All methods specified by the user where recording should start
 */
public final class StartRecordingMethods {

    private final List<MethodMatcher> methods;
    private final List<MethodMatcher> excludeMethods;

    private StartRecordingMethods(List<MethodMatcher> methods, List<MethodMatcher> excludeMethods) {
        this.methods = Collections.unmodifiableList(methods);
        this.excludeMethods = Collections.unmodifiableList(excludeMethods);
    }

    public static StartRecordingMethods parse(String methodsText, String excludeMethodsText) {
        return new StartRecordingMethods(
            CommaSeparatedList.parse(methodsText).stream().map(MethodMatcher::parse).collect(Collectors.toList()),
            CommaSeparatedList.parse(excludeMethodsText).stream().map(MethodMatcher::parse).collect(Collectors.toList())
        );
    }

    public static StartRecordingMethods of(MethodMatcher matcher) {
        return new StartRecordingMethods(Collections.singletonList(matcher), Collections.emptyList());
    }

    public boolean isEmpty() {
        return this.methods.isEmpty();
    }

    public boolean shouldStartRecording(Method description) {
        boolean shouldRecord = methods.isEmpty() || methods.stream().anyMatch(matcher -> matcher.matches(description));

        if (shouldRecord) {
            return excludeMethods.stream().noneMatch(matcher -> matcher.matches(description));
        }

        return false;
    }

    @Override
    public String toString() {
        return methods.toString();
    }
}
