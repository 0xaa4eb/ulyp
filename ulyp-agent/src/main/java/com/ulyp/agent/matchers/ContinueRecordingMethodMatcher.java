package com.ulyp.agent.matchers;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public class ContinueRecordingMethodMatcher implements ElementMatcher<MethodDescription> {



    @Override
    public boolean matches(MethodDescription methodDescription) {
        return false;
    }
}
