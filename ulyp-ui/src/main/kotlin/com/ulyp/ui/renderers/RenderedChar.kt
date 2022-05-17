package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.core.recorders.CharObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RenderedChar internal constructor(value: CharObjectRecord, type: Type?, renderSettings: RenderSettings)
    : RenderedObject(type) {

    init {
        val text: Text = MultilinedText("'" + value.value + "'")
        super.getChildren().add(StyledText.of(text.text, CssClass.CALL_TREE_STRING_LITERAL))
    }
}