package com.ulyp.ui.renderers

import com.ulyp.core.recorders.BooleanRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RecordedBoolean(record: BooleanRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(record.type.name, CssClass.CALL_TREE_TYPE_NAME))
            super.getChildren().add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        super.getChildren().add(of(record.value().toString(), CssClass.CALL_TREE_NUMBER))
    }
}