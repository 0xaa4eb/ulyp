package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text

class RenderedNull internal constructor(renderSettings: RenderSettings?) : RenderedObject(Type.unknown()) {

    init {
        super.getChildren().add(StyledText.of("null", CssClass.CALL_TREE_NULL))
    }
}