package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText

class RenderedFile(filePath: String, type: Type, renderSettings: RenderSettings) : RenderedObject() {

    init {
        if (renderSettings.showTypes) {
            children += StyledText.of(type.name, Style.CALL_TREE_TYPE_NAME)
            children += StyledText.of(": ", Style.CALL_TREE_NODE_SEPARATOR)
        }
        children += StyledText.of(filePath, Style.CALL_TREE_NUMBER)
    }
}