package com.ulyp.ui.renderers

import com.ulyp.ui.util.StyledText.of
import com.ulyp.core.printers.DateRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass

class RenderDate(repr: DateRepresentation, renderSettings: RenderSettings) : RenderedObject(repr.type) {
    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(repr.type.name, CssClass.CALL_TREE_TYPE_NAME))
            super.getChildren().add(of(": ", CssClass.CALL_TREE_PLAIN_TEXT))
        }
        super.getChildren().add(of(repr.datePrinted, CssClass.CALL_TREE_NUMBER))
    }
}