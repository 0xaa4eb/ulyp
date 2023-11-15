package com.agent.tests.util;

import com.ulyp.core.Method;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


public class RecordingMatchers {

    public static Matcher<Method> hasName(String name) {
        return new TypeSafeMatcher<Method>() {
            @Override
            protected boolean matchesSafely(Method item) {
                return name.equals(item.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has method name ").appendValue(name);
            }
        };
    }

    public static Matcher<Method> hasSimpleName(String name) {
        return new TypeSafeMatcher<Method>() {
            @Override
            protected boolean matchesSafely(Method item) {
                return name.equals(item.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has method name ").appendValue(name);
            }
        };
    }

    public static Matcher<CallRecord> hasMethod(Matcher<Method> methodMatcher) {
        return new TypeSafeMatcher<CallRecord>() {
            @Override
            protected boolean matchesSafely(CallRecord item) {
                return methodMatcher.matches(item.getMethod());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has method ").appendDescriptionOf(methodMatcher);
            }
        };
    }

    public static Matcher<CallRecord> hasAtLeastRecordedCalls(int recordedCalls) {
        return new TypeSafeMatcher<CallRecord>() {
            @Override
            protected boolean matchesSafely(CallRecord item) {
                return item.getSubtreeSize() >= recordedCalls;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has at least recorded calls ").appendValue(recordedCalls);
            }
        };
    }

    public static Matcher<CallRecord> hasChildCall(Matcher<CallRecord> recordMatcher) {
        return new TypeSafeMatcher<CallRecord>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("has at least one call that ").appendDescriptionOf(recordMatcher);
            }

            @Override
            protected boolean matchesSafely(CallRecord item) {
                return item.getChildren().stream().anyMatch(recordMatcher::matches);
            }
        };
    }
}
