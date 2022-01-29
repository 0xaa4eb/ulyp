package com.ulyp.core.util;

import com.ulyp.core.Type;

public class ClassMatcher {

    private static final String WILDCARD = "*";

    public static ClassMatcher parse(String text) {
        return new ClassMatcher(text);
    }

    private final String patternText;
    private final AntPathMatcher antMatcher;
    private final boolean isWildcard;

    private ClassMatcher(String patternText) {
        this.antMatcher = new AntPathMatcher(".");

        // '.' is used for nested class name matching instead of '$'
        this.patternText = patternText.replace('$', '.');
        this.isWildcard = patternText.equals(WILDCARD);
    }

    public boolean matches(Type type) {
        if (isWildcard) {
            return true;
        }
        for (String superType : type.getSuperTypeNames()) {
            if (antMatcher.match(patternText, superType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return patternText;
    }
}
