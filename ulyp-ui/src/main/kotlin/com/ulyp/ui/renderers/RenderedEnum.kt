package com.ulyp.ui.renderers

import com.ulyp.core.recorders.EnumRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of

class RenderedEnum(record: EnumRecord, renderSettings: RenderSettings) : RenderedObject(record.type) {
    init {

        val className = if (renderSettings.showTypes()) record.type.name else toSimpleName(record.type.name)

        super.getChildren().addAll(
            listOf(
                of(className, CssClass.CALL_TREE_TYPE_NAME),
                of(".", CssClass.CALL_TREE_NODE_SEPARATOR),
                of(record.name, CssClass.CALL_TREE_NODE_SEPARATOR)
            )
        )
    }
}