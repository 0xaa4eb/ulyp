package com.ulyp.ui.elements.recording.objects

import javafx.scene.text.Text

class TrimmedText(value: String) : Text(trimText(value)) {

    companion object {
        fun trimText(text: String): String {
            var value = text
                    .replace("\t", "\\t")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
            if (value.length > 50) {
                value = value.substring(0..50) + "..."
            }
            return value
        }
    }
}