package com.ulyp.core.util;

import com.ulyp.core.Type;

public class ClassMatcher {

    private static final String WILDCARD = "*";

    public static ClassMatcher parse(String text) {
        return new ClassMatcher(text);
    }

    private final String typeSimpleName;
    private final boolean isWildcard;

    private ClassMatcher(String classSimpleName) {
        this.typeSimpleName = classSimpleName;
        this.isWildcard = classSimpleName.equals(WILDCARD);
    }

    public boolean matches(Type type) {
        return isWildcard || type.getSuperTypeSimpleNames().contains(typeSimpleName);
    }

    @Override
    public String toString() {
        return typeSimpleName;
    }
}
