package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.DateRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RecordedDate(record: DateRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        if (renderSettings.showTypes()) {
            children.add(of(record.type.name, Style.CALL_TREE_TYPE_NAME))
            children.add(of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }
        children.add(of(record.datePrinted, Style.CALL_TREE_NUMBER))
    }
}