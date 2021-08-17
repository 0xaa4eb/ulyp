package com.ulyp.ui.renderers

import com.ulyp.ui.util.StyledText.of
import com.ulyp.core.printers.BooleanRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.renderers.RenderedObject
import com.ulyp.ui.util.StyledText
import com.ulyp.ui.util.CssClass

class RenderedBoolean(representation: BooleanRepresentation, renderSettings: RenderSettings) :
    RenderedObject(representation.type) {
    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(representation.type.name, CssClass.CALL_TREE_TYPE_NAME))
            super.getChildren().add(of(": ", CssClass.CALL_TREE_PLAIN_TEXT))
        }
        super.getChildren().add(of(representation.value().toString(), CssClass.CALL_TREE_NUMBER))
    }
}