package com.ulyp.ui.util

import javafx.scene.text.Text

object StyledText {

    @JvmStatic
    fun of(content: String?, style: CssClass): Text {
        val text = Text(content)
        text.styleClass.addAll(style.cssClasses)
        return text
    }

    @JvmStatic
    fun of(content: String?, vararg styles: CssClass): Text {
        val text = Text(content)
        styles.forEach { text.styleClass.addAll(it.cssClasses) }
        return text
    }
}