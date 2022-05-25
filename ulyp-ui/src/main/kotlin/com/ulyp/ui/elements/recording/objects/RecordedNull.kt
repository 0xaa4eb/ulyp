package com.ulyp.ui.elements.recording.objects

import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText

class RecordedNull internal constructor(renderSettings: RenderSettings?) : RecordedObject() {

    init {
        children.add(StyledText.of("null", Style.CALL_TREE_NULL))
    }
}