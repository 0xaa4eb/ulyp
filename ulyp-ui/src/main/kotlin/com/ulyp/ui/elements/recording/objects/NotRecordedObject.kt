package com.ulyp.ui.elements.recording.objects

import com.ulyp.ui.RenderSettings
import javafx.scene.text.Text

class NotRecordedObject(renderSettings: RenderSettings) : RecordedObjectView() {

    init {
        children.add(if (renderSettings.showTypes) Text("Method has not yet returned any value: ?") else Text("?"))
    }
}