package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.BooleanRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RecordedBooleanView(record: BooleanRecord, renderSettings: RenderSettings) : RecordedObjectView() {

    init {
        if (renderSettings.showTypes) {
            children.add(of(record.type.name, Style.CALL_TREE_TYPE_NAME))
            children.add(of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }
        children.add(of(record.getValue().toString(), Style.CALL_TREE_NUMBER))
    }
}