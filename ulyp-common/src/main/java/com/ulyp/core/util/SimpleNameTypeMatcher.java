package com.ulyp.core.util;

import com.ulyp.core.Type;

class SimpleNameTypeMatcher implements TypeMatcher {

    private final String simpleName;

    SimpleNameTypeMatcher(String simpleName) {
        this.simpleName = simpleName;
    }

    @Override
    public boolean matches(Type type) {
        String simpleName = ClassUtils.getSimpleNameFromName(type.getName());
        if (this.simpleName.equalsIgnoreCase(simpleName)) {
            return true;
        }

        for (String superType : type.getSuperTypeNames()) {
            if (this.simpleName.equalsIgnoreCase(ClassUtils.getSimpleNameFromName(superType))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "**." + simpleName;
    }
}
