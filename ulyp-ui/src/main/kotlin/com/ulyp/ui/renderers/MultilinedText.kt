package com.ulyp.ui.renderers

import javafx.scene.text.Text
import java.lang.StringBuilder

class MultilinedText(value: String) : Text(trimText(value)) {

    companion object {
        fun trimText(text: String): String {
            if (text.length < 100) {
                return text
            }
            val output = StringBuilder(text.length + 10)
            for (i in text.indices) {
                if (i % 100 == 0) {
                    output.append("\n")
                }
                output.append(text[i])
            }
            return output.toString()
        }
    }
}