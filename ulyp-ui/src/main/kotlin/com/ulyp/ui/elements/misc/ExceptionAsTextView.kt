package com.ulyp.ui.elements.misc

import com.ulyp.ui.util.Style
import javafx.scene.text.Text
import java.io.PrintWriter
import java.io.StringWriter


class ExceptionAsTextView(exception: Throwable) : Text() {

    init {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        exception.printStackTrace(printWriter)
        this.text = stringWriter.toString()
        styleClass.addAll(Style.ERROR_TEXT.cssClasses)
    }
}