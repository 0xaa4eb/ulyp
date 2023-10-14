package com.ulyp.ui.elements.recording.objects

import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText

class RenderedNull internal constructor() : RenderedObject() {

    init {
        children.add(StyledText.of("null", Style.CALL_TREE_NULL))
    }
}