package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.core.printers.NumberObjectRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RenderedNumber(
    numberObjectRepresentation: NumberObjectRepresentation,
    type: Type,
    renderSettings: RenderSettings
) : RenderedObject(type) {
    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(type.name, CssClass.CALL_TREE_TYPE_NAME))
            super.getChildren().add(of(": ", CssClass.CALL_TREE_PLAIN_TEXT))
        }
        super.getChildren().add(of(numberObjectRepresentation.numberPrintedText, CssClass.CALL_TREE_NUMBER))
    }
}