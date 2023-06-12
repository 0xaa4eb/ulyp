package com.ulyp.ui.util

import javafx.scene.text.Text

class EnhancedText(private var content: String, vararg styles: Style) : Text(content) {

    init {
        styles.forEach { it -> this.styleClass.addAll(it.cssClasses) }
    }
}