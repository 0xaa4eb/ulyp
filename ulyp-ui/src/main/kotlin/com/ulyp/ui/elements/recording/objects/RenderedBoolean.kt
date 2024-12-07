package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.BooleanRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RenderedBoolean(record: BooleanRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        if (renderSettings.showTypes) {
            children += of(record.type.name, Style.CALL_TREE_TYPE_NAME)
            children += of(": ", Style.CALL_TREE_NODE_SEPARATOR)
        }
        children += of(record.getValue().toString(), Style.CALL_TREE_NUMBER)
    }
}