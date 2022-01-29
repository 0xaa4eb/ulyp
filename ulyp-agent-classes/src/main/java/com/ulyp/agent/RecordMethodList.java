package com.ulyp.agent;

import com.ulyp.core.util.CommaSeparatedList;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.Method;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All methods specified by the user where recording should start
 */
public class RecordMethodList {

    private final List<MethodMatcher> methods;

    public static RecordMethodList parse(String text) {
        return new RecordMethodList(CommaSeparatedList.parse(text).stream().map(MethodMatcher::parse).collect(Collectors.toList()));
    }

    public static RecordMethodList of(MethodMatcher matcher) {
        return new RecordMethodList(Collections.singletonList(matcher));
    }

    private RecordMethodList(List<MethodMatcher> methods) {
        this.methods = methods;
    }

    public boolean isEmpty() {
        return this.methods.isEmpty();
    }

    public boolean shouldStartRecording(Method description) {
        if (methods.isEmpty()) {
            System.out.println("**** EMPTY");
        }
        return methods.isEmpty() || methods.stream().anyMatch(matcher -> matcher.matches(description));
    }

    @Override
    public String toString() {
        return methods.toString();
    }
}
