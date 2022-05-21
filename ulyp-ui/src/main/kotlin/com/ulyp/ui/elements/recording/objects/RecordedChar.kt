package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.core.recorders.CharObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RecordedChar internal constructor(value: CharObjectRecord, type: Type?, renderSettings: RenderSettings)
    : RecordedObject(type) {

    init {
        val text: Text = MultilinedText("'" + value.value + "'")
        children.add(StyledText.of(text.text, Style.CALL_TREE_STRING_LITERAL))
    }
}