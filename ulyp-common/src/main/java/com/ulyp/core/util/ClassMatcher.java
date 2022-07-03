package com.ulyp.core.util;

import com.ulyp.core.Type;

public class ClassMatcher {

    private static final String WILDCARD = "*";

    private final String patternText;
    private final String rawPatternText;
    private final AntPathMatcher antMatcher;
    private final boolean isWildcard;

    private ClassMatcher(String patternText) {
        this.antMatcher = new AntPathMatcher(".");

        this.rawPatternText = patternText;
        this.patternText = patternText.replace('$', '.');
        this.isWildcard = patternText.equals(WILDCARD);
    }

    public static ClassMatcher parse(String text) {
        return new ClassMatcher(text);
    }

    public boolean matches(Type type) {

        // TODO match by subclass
        if (isWildcard) {
            return true;
        }

        String nameToCheckAgainst = type.getName();
        if (type.getName().contains("$")) {
            nameToCheckAgainst = nameToCheckAgainst.replace('$', '.');
        }
        if (antMatcher.match(patternText, nameToCheckAgainst)) {
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
        return rawPatternText;
    }
}
