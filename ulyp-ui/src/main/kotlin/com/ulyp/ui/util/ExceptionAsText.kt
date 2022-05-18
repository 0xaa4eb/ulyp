package com.ulyp.ui.util

import javafx.scene.text.Text
import java.io.PrintWriter
import java.io.StringWriter


class ExceptionAsText(exception: Exception): Text() {

    init {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        exception.printStackTrace(printWriter)
        this.text = stringWriter.toString()
        styleClass.addAll(CssClass.ERROR_TEXT.cssClasses)
    }
}