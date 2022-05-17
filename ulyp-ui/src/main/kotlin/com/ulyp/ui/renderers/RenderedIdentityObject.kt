package com.ulyp.ui.renderers

import com.ulyp.core.recorders.IdentityObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RenderedIdentityObject(record: IdentityObjectRecord, renderSettings: RenderSettings) : RenderedObject(record.type) {

    init {
        val className = if (renderSettings.showTypes()) record.type.name else toSimpleName(record.type.name)

        super.getChildren().addAll(
            listOf(
                of(className, CssClass.CALL_TREE_TYPE_NAME),
                of("@", CssClass.CALL_TREE_IDENTITY, CssClass.CALL_TREE_IDENTITY_HASH_CODE),
                of(Integer.toHexString(record.hashCode), CssClass.CALL_TREE_IDENTITY)
            )
        )
    }
}