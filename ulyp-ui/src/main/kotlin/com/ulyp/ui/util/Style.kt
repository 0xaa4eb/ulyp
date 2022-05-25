package com.ulyp.ui.util

import java.util.*

enum class Style(vararg classes: String) {

    ERROR_TEXT("ulyp-error-text"),
    HELP_TEXT("ulyp-help-text"),
    CALL_TREE("ulyp-ctt"),
    CALL_TREE_TYPE_NAME("ulyp-ctt-typename"),
    CALL_TREE_STRING_LITERAL("ulyp-ctt-string"),
    CALL_TREE_NUMBER("ulyp-ctt-number"),
    CALL_TREE_NULL("ulyp-ctt-null"),
    CALL_TREE_NODE_SEPARATOR("ulyp-ctt-sep"),
    CALL_TREE_COLLECTION_BRACKET("ulyp-ctt-bracket"),
    CALL_TREE_IDENTITY("ulyp-ctt-identity"),
    CALL_TREE_IDENTITY_HASH_CODE("ulyp-ctt-identity-hash-code"),
    CALL_TREE_METHOD_NAME("ulyp-ctt-method-name"),
    CALL_TREE_RETURN_VALUE("ulyp-ctt-return-value"),
    CALL_TREE_THROWN("ulyp-ctt-thrown"),
    CALL_TREE_CALLEE("ulyp-ctt-callee");

    val cssClasses: List<String>

    init {
        cssClasses = listOf(*classes)
    }
}