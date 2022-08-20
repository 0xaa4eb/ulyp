package com.ulyp.ui.util

import javafx.scene.text.Text

/**
 * Replaces most special symbols with their text representation. This makes
 * a text single lined, which is what needed in a call tree
 */
class SingleLineTextView(value: Text) : Text() {

    init {
        this.text = value.text
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
    }
}