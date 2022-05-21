package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText

class RecordedNumber(numberPrinted: String, type: Type, renderSettings: RenderSettings) : RecordedObject(type) {

    init {
        if (renderSettings.showTypes()) {
            children.add(StyledText.of(type.name, Style.CALL_TREE_TYPE_NAME))
            children.add(StyledText.of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }
        children.add(StyledText.of(numberPrinted, Style.CALL_TREE_NUMBER))
    }
}