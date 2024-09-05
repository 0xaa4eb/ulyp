package com.ulyp.core.util;

import com.ulyp.core.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

/**
 * Compound matcher which allows to specify multiple method matchers as comma separated list:
 * **.Class1.run,**.Class2.run
 * It's also possible to specify methods to exclude via '-' sign as follows:
 * **.Class1.run,**.Class2.run,-**.Class3.run
 * It's usually used to specify at which methods recording should start.
 */
public final class CompoundMethodMatcher implements MethodMatcher {

    private final List<MethodMatcher> matchers;
    private final List<MethodMatcher> excludeMatchers;

    private CompoundMethodMatcher(List<MethodMatcher> matchers, List<MethodMatcher> excludeMatchers) {
        this.matchers = Collections.unmodifiableList(matchers);
        this.excludeMatchers = Collections.unmodifiableList(excludeMatchers);
    }

    public static CompoundMethodMatcher parse(String methodsText) {
        StringTokenizer stringTokenizer = new StringTokenizer(methodsText, String.valueOf(MATCHER_SEPARATOR));
        List<MethodMatcher> matchers = new ArrayList<>();
        List<MethodMatcher> excludeMatchers = new ArrayList<>();

        while (stringTokenizer.hasMoreTokens()) {
            String nextMatcherText = stringTokenizer.nextToken();
            if (nextMatcherText.startsWith(EXCLUDE_METHOD_PREFIX)) {
                excludeMatchers.add(MethodMatcher.parse(nextMatcherText.substring(EXCLUDE_METHOD_PREFIX.length())));
            } else {
                matchers.add(MethodMatcher.parse(nextMatcherText));
            }
        }
        if (matchers.isEmpty()) {
            throw new IllegalArgumentException("Parse failed. At least one method to match must be included. " +
                    "Provided: '" + methodsText + "'");
        }
        return new CompoundMethodMatcher(matchers, excludeMatchers);
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
        StringBuilder builder = new StringBuilder(matchers.stream().map(MethodMatcher::toString).collect(Collectors.joining(",")));
        if (!excludeMatchers.isEmpty()) {
            builder.append(",").append(excludeMatchers.stream().map(excludeMatcher -> "-" + excludeMatcher.toString()).collect(Collectors.joining(",")));
        }
        return builder.toString();
    }
}
