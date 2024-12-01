package com.ulyp.core.util;

import com.ulyp.core.Type;

class AcceptAllTypeMatcher implements TypeMatcher {

    @Override
    public boolean matches(Type type) {
        return true;
    }

    public String toString() {
        return WILDCARD;
    }
}
