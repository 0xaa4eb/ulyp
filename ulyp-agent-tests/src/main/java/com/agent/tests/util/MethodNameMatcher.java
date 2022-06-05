package com.agent.tests.util;

import com.ulyp.core.Method;
import lombok.AllArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class MethodNameMatcher extends TypeSafeMatcher<Method> {

    @NotNull
    private final String nameToMatch;

    @Override
    protected boolean matchesSafely(Method item) {
        return item.getName().equals(nameToMatch);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("has method name").appendValue(nameToMatch);
    }
}
