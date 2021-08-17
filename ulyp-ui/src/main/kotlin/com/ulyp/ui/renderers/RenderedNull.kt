package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import javafx.scene.text.Text

class RenderedNull internal constructor(renderSettings: RenderSettings?) : RenderedObject(Type.unknown()) {
    init {
        super.getChildren().add(Text("null"))
    }
}