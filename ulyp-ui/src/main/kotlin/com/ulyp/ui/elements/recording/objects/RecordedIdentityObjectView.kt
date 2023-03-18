package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.IdentityObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RecordedIdentityObjectView(record: IdentityObjectRecord, renderSettings: RenderSettings) : RecordedObjectView() {

    init {
        val className = if (renderSettings.showTypes) record.type.name else toSimpleName(record.type.name)

        children.addAll(
                listOf(
                        of(className, Style.CALL_TREE_TYPE_NAME),
                        of("@", Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT),
                        of(Integer.toHexString(record.hashCode), Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT)
                )
        )
    }
}