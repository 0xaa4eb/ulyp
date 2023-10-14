package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.ClassObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import com.ulyp.ui.util.TrimmedTextView
import javafx.scene.text.Text

class RenderedClassObject(record: ClassObjectRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        if (renderSettings.showTypes) {
            children.add(of(Class::class.java.name + ": ", Style.CALL_TREE_TYPE_NAME))
        }
        val text: Text = TrimmedTextView(Text("class " + record.carriedType.name))
        children.add(text)
    }
}