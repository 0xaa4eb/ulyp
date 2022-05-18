package com.ulyp.ui.util

import java.util.*

enum class CssClass(vararg classes: String) {

    TEXT("ulyp-ctt-sep"),
    CALL_TREE_ALL("ulyp-ctt"),
    CALL_TREE_TYPE_NAME("ulyp-ctt-typename"),
    CALL_TREE_STRING_LITERAL("ulyp-ctt-string"),
    CALL_TREE_NUMBER("ulyp-ctt-number"),
    CALL_TREE_FILE("ulyp-ctt-file"),
    CALL_TREE_NULL("ulyp-ctt-null"),
    CALL_TREE_NODE_SEPARATOR("ulyp-ctt-sep"),
    CALL_TREE_COLLECTION_BRACKET("ulyp-ctt-bracket"),
    CALL_TREE_IDENTITY("ulyp-ctt-identity"),
    CALL_TREE_IDENTITY_HASH_CODE("ulyp-ctt-identity-hash-code"),
    CALL_TREE_METHOD_NAME("ulyp-ctt-method-name"),
    CALL_TREE_RETURN_VALUE("ulyp-ctt-return-value"),
    CALL_TREE_THROWN("ulyp-ctt-thrown"),
    CALL_TREE_ARG_NAME("ulyp-ctt-arg-name"),
    CALL_TREE_CALLEE("ulyp-ctt-callee");

    val cssClasses: List<String>

    init {
        cssClasses = Arrays.asList(*classes)
    }
}