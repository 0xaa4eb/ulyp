package com.ulyp.core.util;

import com.ulyp.core.Type;

public interface TypeMatcher {

    String WILDCARD = "*";

    static TypeMatcher parse(String text) {
        if (text.startsWith(">")) {
            return new SimpleNameTypeMatcher(text.substring(1));
        } else if (text.equals(WILDCARD)) {
            return new AcceptAllTypeMatcher();
        } else {
            return new AntPatternTypeMatcher(text);
        }
    }

    boolean matches(Type type);

    class AcceptAllTypeMatcher implements TypeMatcher {

        @Override
        public boolean matches(Type type) {
            return true;
        }

        public String toString() {
            return WILDCARD;
        }
    }

    class AntPatternTypeMatcher implements TypeMatcher {

        private final String patternText;
        private final String rawPatternText;
        private final AntPathMatcher antMatcher;

        public AntPatternTypeMatcher(String patternText) {
            this.antMatcher = new AntPathMatcher(".");
            this.rawPatternText = patternText;
            this.patternText = patternText.replace('$', '.');
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

    class SimpleNameTypeMatcher implements TypeMatcher {

        private final String simpleName;

        public SimpleNameTypeMatcher(String simpleName) {
            this.simpleName = simpleName;
        }

        @Override
        public boolean matches(Type type) {
            String nameToCheckAgainst = type.getName();
            if (type.getName().contains("$")) {
                nameToCheckAgainst = nameToCheckAgainst.replace('$', '.');
            }
            int lastDotIdx = nameToCheckAgainst.lastIndexOf('.');
            if (lastDotIdx >= 0) {
                nameToCheckAgainst = nameToCheckAgainst.substring(lastDotIdx + 1);
            }
            return simpleName.equalsIgnoreCase(nameToCheckAgainst);
        }

        @Override
        public String toString() {
            return ">" + simpleName;
        }
    }
}
