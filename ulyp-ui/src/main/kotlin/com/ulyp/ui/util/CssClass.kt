package com.ulyp.ui.util

import java.util.*

enum class CssClass(vararg classes: String) {

    ERROR_TEXT_CSS("ulyp-error-text"),
    HELP_TEXT_CSS("ulyp-help-text"),
    CALL_TREE_ALL_CSS("ulyp-ctt"),
    CALL_TREE_TYPE_NAME_CSS("ulyp-ctt-typename"),
    CALL_TREE_STRING_LITERAL_CSS("ulyp-ctt-string"),
    CALL_TREE_NUMBER_CSS("ulyp-ctt-number"),
    CALL_TREE_FILE_CSS("ulyp-ctt-file"),
    CALL_TREE_NULL_CSS("ulyp-ctt-null"),
    CALL_TREE_NODE_SEPARATOR_CSS("ulyp-ctt-sep"),
    CALL_TREE_COLLECTION_BRACKET_CSS("ulyp-ctt-bracket"),
    CALL_TREE_IDENTITY_CSS("ulyp-ctt-identity"),
    CALL_TREE_IDENTITY_HASH_CODE_CSS("ulyp-ctt-identity-hash-code"),
    CALL_TREE_METHOD_NAME_CSS("ulyp-ctt-method-name"),
    CALL_TREE_RETURN_VALUE_CSS("ulyp-ctt-return-value"),
    CALL_TREE_THROWN_CSS("ulyp-ctt-thrown"),
    CALL_TREE_ARG_NAME_CSS("ulyp-ctt-arg-name"),
    CALL_TREE_CALLEE_CSS("ulyp-ctt-callee");

    val cssClasses: List<String>
        get() = field

    init {
        cssClasses = Arrays.asList(*classes)
    }
}