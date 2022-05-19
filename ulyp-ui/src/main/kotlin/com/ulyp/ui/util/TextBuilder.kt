package com.ulyp.ui.util

import javafx.scene.text.Text
import java.util.function.Consumer

class TextBuilder {

    private var text: String? = null
    private val clazzes: MutableList<String> = ArrayList()

    fun text(text: String?): TextBuilder {
        this.text = text
        return this
    }

    fun style(clazz: String): TextBuilder {
        clazzes.add(clazz)
        return this
    }

    fun style(clazz: Style): TextBuilder {
        clazzes.addAll(clazz.cssClasses)
        return this
    }

    fun build(): Text {
        val te = Text(text)
        clazzes.forEach(Consumer { cl: String? -> te.styleClass.add(cl) })
        return te
    }
}