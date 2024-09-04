package com.ulyp.core.util;

import com.ulyp.core.Method;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compound matcher which allows to specify multiple method matchers. It's usually
 * used to specify at which methods recording should start.
 */
public final class CompoundMethodMatcher implements MethodMatcher {

    private final List<MethodMatcher> matchers;
    private final List<MethodMatcher> excludeMatchers;

    private CompoundMethodMatcher(List<MethodMatcher> matchers, List<MethodMatcher> excludeMatchers) {
        this.matchers = Collections.unmodifiableList(matchers);
        this.excludeMatchers = Collections.unmodifiableList(excludeMatchers);
    }

    public static CompoundMethodMatcher parse(String methodsText) {
        // TODO implement exclude methods
        return new CompoundMethodMatcher(
                CommaSeparatedList.parse(methodsText).stream().map(MethodMatcher::parse).collect(Collectors.toList()),
                Collections.emptyList()
        );
    }

    public static CompoundMethodMatcher of(MethodMatcher matcher) {
        return new CompoundMethodMatcher(Collections.singletonList(matcher), Collections.emptyList());
    }

    public boolean isEmpty() {
        return this.matchers.isEmpty();
    }

    public boolean matches(Method description) {
        boolean shouldRecord = matchers.isEmpty() || matchers.stream().anyMatch(matcher -> matcher.matches(description));

        if (shouldRecord) {
            return excludeMatchers.stream().noneMatch(matcher -> matcher.matches(description));
        }

        return false;
    }

    @Override
    public String toString() {
        return "CompoundMethodMatcher{" +
                "matchers=" + matchers +
                ", excludeMatchers=" + excludeMatchers +
                '}';
    }
}
