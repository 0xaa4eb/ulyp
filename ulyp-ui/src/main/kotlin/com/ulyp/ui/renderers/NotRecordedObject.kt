package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import javafx.scene.text.Text

class NotRecordedObject(renderSettings: RenderSettings) : RecordedObject(Type.unknown()) {

    init {
        super.getChildren().add(if (renderSettings.showTypes()) Text("Method has not yet returned any value: ?") else Text("?"))
    }
}