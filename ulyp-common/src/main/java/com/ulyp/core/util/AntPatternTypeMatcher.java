package com.ulyp.core.util;

import com.ulyp.core.Type;

class AntPatternTypeMatcher implements TypeMatcher {

    private final String patternText;
    private final String rawPatternText;
    private final AntPathMatcher antMatcher;

    AntPatternTypeMatcher(String patternText) {
        this.antMatcher = new AntPathMatcher(".");
        this.rawPatternText = patternText;
        this.patternText = patternText.replace('$', '.');
    }

    static TypeMatcher of(String patternText) {
        patternText = patternText.replace('$', '.');

        if (patternText.startsWith("**.") && patternText.lastIndexOf('.') == 2) {
            // formats like **.ABC can be matched by simple class name type matchers
            return new SimpleNameTypeMatcher(patternText.substring(3));
        } else {
            return new com.ulyp.core.util.AntPatternTypeMatcher(patternText);
        }
    }

    @Override
    public boolean matches(Type type) {
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
