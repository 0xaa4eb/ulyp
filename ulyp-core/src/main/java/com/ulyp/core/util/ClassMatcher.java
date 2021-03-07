package com.ulyp.core.util;

import com.ulyp.core.printers.TypeInfo;

public class ClassMatcher {

    private static final String WILDCARD = "*";

    public static ClassMatcher parse(String text) {
        return new ClassMatcher(text);
    }

    private final String classSimpleName;
    private final boolean isWildcard;

    private ClassMatcher(String classSimpleName) {
        this.classSimpleName = classSimpleName;
        this.isWildcard = classSimpleName.equals(WILDCARD);
    }

    public boolean matches(TypeInfo type) {
        return isWildcard || (type.getInterfacesSimpleClassNames().contains(classSimpleName) || type.getSuperClassesSimpleNames().contains(classSimpleName));
    }

    @Override
    public String toString() {
        return classSimpleName;
    }
}
