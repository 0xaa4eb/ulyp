package com.ulyp.ui.util

import javafx.scene.text.Text

class TrimmedTextView(value: Text) : Text() {

    init {
        var textContent = value.text
        if (textContent.length > 75) {
            textContent = textContent.substring(0..75) + "..."
        }
        this.text = textContent
    }
}