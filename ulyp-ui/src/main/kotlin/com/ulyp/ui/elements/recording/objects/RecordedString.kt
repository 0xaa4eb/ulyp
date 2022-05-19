package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.core.recorders.StringObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RecordedString internal constructor(record: StringObjectRecord, classDescription: Type?, renderSettings: RenderSettings?)
    : RecordedObject(classDescription) {

    init {
        val text: Text = MultilinedText("\"" + record.value() + "\"")
        super.getChildren().add(StyledText.of(text.text, Style.CALL_TREE_STRING_LITERAL))
    }
}