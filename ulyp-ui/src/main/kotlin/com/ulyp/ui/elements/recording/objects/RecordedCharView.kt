package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.CharObjectRecord
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import com.ulyp.ui.util.TrimmedTextView
import javafx.scene.text.Text

class RecordedCharView internal constructor(value: CharObjectRecord)
    : RecordedObjectView() {

    init {
        val text: Text = TrimmedTextView(Text("'" + value.value + "'"))
        children.add(StyledText.of(text.text, Style.CALL_TREE_STRING))
    }
}