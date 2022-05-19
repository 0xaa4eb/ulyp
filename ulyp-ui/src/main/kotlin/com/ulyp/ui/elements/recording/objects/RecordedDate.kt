package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.DateRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RecordedDate(record: DateRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(record.type.name, CssClass.CALL_TREE_TYPE_NAME_CSS))
            super.getChildren().add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR_CSS))
        }
        super.getChildren().add(of(record.datePrinted, CssClass.CALL_TREE_NUMBER_CSS))
    }
}