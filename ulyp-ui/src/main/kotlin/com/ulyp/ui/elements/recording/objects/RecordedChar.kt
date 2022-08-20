package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.CharObjectRecord
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RecordedChar internal constructor(value: CharObjectRecord)
    : RecordedObject() {

    init {
        val text: Text = TrimmedText("'" + value.value + "'")
        children.add(StyledText.of(text.text, Style.CALL_TREE_STRING_LITERAL))
    }
}