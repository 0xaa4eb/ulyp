package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.renderers.RenderedObject
import javafx.scene.text.Text

class RenderedNotRecordedObject(renderSettings: RenderSettings) : RenderedObject(Type.unknown()) {
    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(Text("Method has not yet returned any value: ?"))
        } else {
            super.getChildren().add(Text("?"))
        }
    }
}