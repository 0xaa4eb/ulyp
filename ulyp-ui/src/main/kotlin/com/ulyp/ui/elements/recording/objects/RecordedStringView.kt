package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.StringObjectRecord
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RecordedStringView internal constructor(record: StringObjectRecord) : RecordedObjectView() {

    init {
        val text: Text = TrimmedTextView("\"" + record.value() + "\"")
        children.add(StyledText.of(text.text, Style.CALL_TREE_STRING_LITERAL))
    }
}