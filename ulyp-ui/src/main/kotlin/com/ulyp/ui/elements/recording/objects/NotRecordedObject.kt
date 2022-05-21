package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.Type
import com.ulyp.ui.RenderSettings
import javafx.scene.text.Text

class NotRecordedObject(renderSettings: RenderSettings) : RecordedObject(Type.unknown()) {

    init {
        children.add(if (renderSettings.showTypes()) Text("Method has not yet returned any value: ?") else Text("?"))
    }
}