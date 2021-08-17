package com.ulyp.ui.renderers

import com.ulyp.core.printers.IdentityObjectRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import java.util.*

class RenderedIdentityObject(repr: IdentityObjectRepresentation, renderSettings: RenderSettings) :
    RenderedObject(repr.type) {
    init {
        val className = if (renderSettings.showTypes()) repr.type.name else toSimpleName(repr.type.name)
        super.getChildren().addAll(
            Arrays.asList(
                of(className, CssClass.CALL_TREE_TYPE_NAME),
                of("@", CssClass.CALL_TREE_IDENTITY_REPR),
                of(Integer.toHexString(repr.hashCode), CssClass.CALL_TREE_IDENTITY_REPR)
            )
        )
    }
}