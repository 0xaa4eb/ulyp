package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.DateRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RecordedDate(record: DateRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(record.type.name, Style.CALL_TREE_TYPE_NAME))
            super.getChildren().add(of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }
        super.getChildren().add(of(record.datePrinted, Style.CALL_TREE_NUMBER))
    }
}