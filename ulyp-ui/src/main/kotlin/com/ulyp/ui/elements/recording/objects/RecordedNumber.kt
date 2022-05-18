package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText

class RecordedNumber(numberPrinted: String, type: Type, renderSettings: RenderSettings) : RecordedObject(type) {

    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(StyledText.of(type.name, CssClass.CALL_TREE_TYPE_NAME))
            super.getChildren().add(StyledText.of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        super.getChildren().add(StyledText.of(numberPrinted, CssClass.CALL_TREE_NUMBER))
    }
}