package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.EnumRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RenderedEnum(record: EnumRecord, renderSettings: RenderSettings) : RenderedObject() {
    init {

        val className = if (renderSettings.showTypes) record.type.name else toSimpleName(record.type.name)

        children.addAll(
                listOf(
                        of(className, Style.CALL_TREE_TYPE_NAME),
                        of(".", Style.CALL_TREE_NODE_SEPARATOR),
                        of(record.name, Style.CALL_TREE_NODE_SEPARATOR)
                )
        )
    }
}