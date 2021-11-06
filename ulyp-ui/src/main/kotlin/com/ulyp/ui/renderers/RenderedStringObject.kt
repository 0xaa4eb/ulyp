package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.core.recorders.StringObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RenderedStringObject internal constructor(representation: StringObjectRecord, classDescription: Type?, renderSettings: RenderSettings?)
    : RenderedObject(classDescription) {

    init {
        val text: Text = MultilinedText("\"" + representation.value() + "\"")
        super.getChildren().add(StyledText.of(text.text, CssClass.CALL_TREE_STRING_LITERAL))
    }
}