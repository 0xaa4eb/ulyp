package com.ulyp.ui.renderers

import com.ulyp.core.recorders.IdentityObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RenderedIdentityObject(repr: IdentityObjectRecord, renderSettings: RenderSettings) : RenderedObject(repr.type) {

    init {
        val className = if (renderSettings.showTypes()) repr.type.name else toSimpleName(repr.type.name)

        super.getChildren().addAll(
            listOf(
                of(className, CssClass.CALL_TREE_TYPE_NAME),
                of("@", CssClass.CALL_TREE_IDENTITY_REPR),
                of(Integer.toHexString(repr.hashCode), CssClass.CALL_TREE_IDENTITY_REPR)
            )
        )
    }
}