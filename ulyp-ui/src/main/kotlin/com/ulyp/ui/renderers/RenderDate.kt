package com.ulyp.ui.renderers

import com.ulyp.core.recorders.DateRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RenderDate(record: DateRecord, renderSettings: RenderSettings) : RenderedObject(record.type) {

    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(record.type.name, CssClass.CALL_TREE_TYPE_NAME))
            super.getChildren().add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        super.getChildren().add(of(record.datePrinted, CssClass.CALL_TREE_NUMBER))
    }
}