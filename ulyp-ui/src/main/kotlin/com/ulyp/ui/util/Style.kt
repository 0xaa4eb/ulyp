package com.ulyp.ui.util

enum class Style(vararg classes: String) {

    TOOLTIP_TEXT("ulyp-tooltip-text"),
    ERROR_TEXT("ulyp-error-text"),
    HELP_TEXT("ulyp-help-text"),
    CALL_TREE("ulyp-call-tree"),
    CALL_TREE_TYPE_NAME("ulyp-call-tree-typename"),
    CALL_TREE_STRING("ulyp-call-tree-string"),
    CALL_TREE_NUMBER("ulyp-call-tree-number"),
    CALL_TREE_NULL("ulyp-call-tree-null"),
    CALL_TREE_NODE_SEPARATOR("ulyp-call-tree-sep"),
    CALL_TREE_COLLECTION_BRACKET("ulyp-call-tree-bracket"),
    CALL_TREE_IDENTITY("ulyp-call-tree-identity"),
    CALL_TREE_IDENTITY_HASH_CODE("ulyp-call-tree-identity-hash-code"),
    CALL_TREE_METHOD_NAME("ulyp-call-tree-method-name"),
    CALL_TREE_RETURN_VALUE("ulyp-call-tree-return-value"),
    CALL_TREE_THROWN("ulyp-call-tree-thrown"),
    CALL_TREE_CALLEE("ulyp-call-tree-callee");

    val cssClasses: List<String>

    init {
        cssClasses = listOf(*classes)
    }
}