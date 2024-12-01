package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.MethodRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import com.ulyp.ui.util.TrimmedTextView
import java.lang.reflect.Method

class RenderedMethod(record: MethodRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        if (renderSettings.showTypes) {
            children.add(of(Method::class.java.name + ": ", Style.CALL_TREE_TYPE_NAME))
        }

        children.addAll(
            listOf(
                TrimmedTextView(of(record.declaringType.name, Style.CALL_TREE_CLASS, Style.CALL_TREE_BOLD)),
                of("#", Style.CALL_TREE_CLASS, Style.SMALLER_TEXT, Style.CALL_TREE_BOLD),
                of(record.name, Style.CALL_TREE_CLASS, Style.SMALLER_TEXT)
            )
        )
    }
}