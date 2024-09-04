package com.ulyp.agent.options;

import com.ulyp.core.util.MethodMatcher;

import java.util.ArrayList;
import java.util.List;

public class MethodMatchersParser implements Parser<MethodMatcher> {

    private static final String EXCLUDE_SIGN = "-";

    private final ListParser<String> listParser = new ListParser<>(text -> text);

    @Override
    public MethodMatcher parse(String text) {
        List<String> methodMatchersSplit = listParser.parse(text);
        List<MethodMatcher> methodMatchers = new ArrayList<>();
        List<MethodMatcher> excludeMethodMatchers = new ArrayList<>();

        for (String methodMatcherText : methodMatchersSplit) {
            if (methodMatcherText.startsWith(EXCLUDE_SIGN)) {
                excludeMethodMatchers.add(MethodMatcher.parse(methodMatcherText.substring(EXCLUDE_SIGN.length())));
            } else {
                methodMatchers.add(MethodMatcher.parse(methodMatcherText));
            }
        }

        return MethodMatcher.parse(text);
    }
}
