package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText

class RecordedNull internal constructor(renderSettings: RenderSettings?) : RecordedObject(Type.unknown()) {

    init {
        super.getChildren().add(StyledText.of("null", CssClass.CALL_TREE_NULL_CSS))
    }
}