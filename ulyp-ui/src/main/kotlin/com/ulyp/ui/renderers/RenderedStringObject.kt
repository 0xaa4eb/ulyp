package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.core.printers.StringObjectRepresentation
import com.ulyp.ui.RenderSettings
import javafx.scene.text.Text

class RenderedStringObject internal constructor(
    representation: StringObjectRepresentation,
    classDescription: Type?,
    renderSettings: RenderSettings?
) : RenderedObject(classDescription) {
    init {
        val text: Text = MultilinedText("\"" + representation.value() + "\"")
        text.styleClass.add("ulyp-ctt-string")
        super.getChildren().add(text)
    }
}