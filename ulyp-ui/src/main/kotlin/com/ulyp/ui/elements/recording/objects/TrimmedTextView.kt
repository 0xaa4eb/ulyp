package com.ulyp.ui.elements.recording.objects

import javafx.scene.text.Text

class TrimmedTextView(value: String) : Text() {

    init {
        var trimmed = value
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
        if (trimmed.length > 50) {
            trimmed = trimmed.substring(0..50) + "..."
        }
        this.text = trimmed
    }
}