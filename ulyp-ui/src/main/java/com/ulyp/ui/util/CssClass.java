package com.ulyp.ui.util;

import java.util.Arrays;
import java.util.List;

public enum CssClass {

    CALL_TREE_TYPE_NAME("ulyp-ctt", "ulyp-ctt-typename"),
    CALL_TREE_STRING_LITERAL("ulyp-ctt", "ulyp-ctt-string"),
    CALL_TREE_NUMBER("ulyp-ctt", "ulyp-ctt-number"),
    CALL_TREE_PLAIN_TEXT("ulyp-ctt", "ulyp-ctt-sep"),
    CALL_TREE_COLLECTION_BRACE("ulyp-ctt", "ulyp-ctt-brace"),
    CALL_TREE_IDENTITY_REPR("ulyp-ctt", "ulyp-ctt-identity"),
    CALL_TREE_METHOD_NAME("ulyp-ctt", "ulyp-ctt-method-name"),
    CALL_TREE_RETURN_VALUE("ulyp-ctt", "ulyp-ctt-return-value"),
    CALL_TREE_THROWN("ulyp-ctt", "ulyp-ctt-thrown"),
    CALL_TREE_ARG_NAME("ulyp-ctt", "ulyp-ctt-arg-name"),
    CALL_TREE_CALLEE("ulyp-ctt", "ulyp-ctt-callee");

    private final List<String> classes;

    CssClass(String... classes) {
        this.classes = Arrays.asList(classes);
    }

    public List<String> getCssClasses() {
        return classes;
    }
}
