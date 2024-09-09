package com.ulyp.core.util;

import com.ulyp.core.Type;

public interface TypeMatcher {

    String WILDCARD = "*";
    String DOUBLE_WILDCARD = "**";

    static TypeMatcher parse(String text) {
        if (text.equals(WILDCARD) || text.equals(DOUBLE_WILDCARD)) {
            return new AcceptAllTypeMatcher();
        } else {
            return AntPatternTypeMatcher.of(text);
        }
    }

    boolean matches(Type type);
}
