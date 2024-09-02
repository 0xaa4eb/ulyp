package com.ulyp.agent.options;

import com.ulyp.core.util.TypeMatcher;

public class TypeMatcherParser implements Parser<TypeMatcher> {

    @Override
    public TypeMatcher parse(String text) {
        // TODO have some validation
        return TypeMatcher.parse(text);
    }
}
