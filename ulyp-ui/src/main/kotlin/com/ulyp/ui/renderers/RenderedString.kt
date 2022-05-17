package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.core.recorders.StringObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RenderedString internal constructor(record: StringObjectRecord, classDescription: Type?, renderSettings: RenderSettings?)
    : RenderedObject(classDescription) {

    init {
        val text: Text = MultilinedText("\"" + record.value() + "\"")
        super.getChildren().add(StyledText.of(text.text, CssClass.CALL_TREE_STRING_LITERAL))
    }
}