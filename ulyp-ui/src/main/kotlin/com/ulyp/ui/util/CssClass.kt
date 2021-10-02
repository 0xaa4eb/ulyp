package com.ulyp.ui.util

import java.util.*

enum class CssClass(vararg classes: String) {

    CALL_TREE_TYPE_NAME("ulyp-ctt", "ulyp-ctt-typename"),
    CALL_TREE_STRING_LITERAL(
        "ulyp-ctt",
        "ulyp-ctt-string"
    ),
    CALL_TREE_NUMBER("ulyp-ctt", "ulyp-ctt-number"),
    CALL_TREE_PLAIN_TEXT(
        "ulyp-ctt",
        "ulyp-ctt-sep"
    ),
    CALL_TREE_COLLECTION_BRACKET("ulyp-ctt", "ulyp-ctt-bracket"),
    CALL_TREE_IDENTITY_REPR(
        "ulyp-ctt",
        "ulyp-ctt-identity"
    ),
    CALL_TREE_METHOD_NAME("ulyp-ctt", "ulyp-ctt-method-name"),
    CALL_TREE_RETURN_VALUE(
        "ulyp-ctt",
        "ulyp-ctt-return-value"
    ),
    CALL_TREE_THROWN("ulyp-ctt", "ulyp-ctt-thrown"),
    CALL_TREE_ARG_NAME("ulyp-ctt", "ulyp-ctt-arg-name"),
    CALL_TREE_CALLEE("ulyp-ctt", "ulyp-ctt-callee");

    val cssClasses: List<String>

    init {
        cssClasses = Arrays.asList(*classes)
    }
}