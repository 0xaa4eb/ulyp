package com.ulyp.ui.settings

/**
 * Every call in a call tree has weight. It's drawn as a rectangle in a background.
 * It represents how much time/calls current call's subtree has in comparison to other calls. Currently, weight can
 * be either time spent or call count.
 */
enum class RecordedCallWeightType {
    TIME,
    CALLS
}