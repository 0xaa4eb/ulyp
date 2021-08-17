package com.ulyp.ui.renderers

import com.ulyp.core.printers.EnumRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RenderedEnum(representation: EnumRepresentation, renderSettings: RenderSettings) :
    RenderedObject(representation.type) {
    init {

        val className = if (renderSettings.showTypes()) representation.type.name else toSimpleName(representation.type.name)

        super.getChildren().addAll(
            listOf(
                of(className, CssClass.CALL_TREE_TYPE_NAME),
                of(".", CssClass.CALL_TREE_PLAIN_TEXT),
                of(representation.name, CssClass.CALL_TREE_PLAIN_TEXT)
            )
        )
    }
}