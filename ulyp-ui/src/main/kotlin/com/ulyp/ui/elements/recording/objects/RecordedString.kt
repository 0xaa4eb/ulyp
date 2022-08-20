package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.StringObjectRecord
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RecordedString internal constructor(record: StringObjectRecord) : RecordedObject() {

    init {
        val text: Text = TrimmedText("\"" + record.value() + "\"")
        children.add(StyledText.of(text.text, Style.CALL_TREE_STRING_LITERAL))
    }
}