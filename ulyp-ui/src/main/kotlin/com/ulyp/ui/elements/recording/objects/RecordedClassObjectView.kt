package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.ClassObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.text.Text

class RecordedClassObjectView(record: ClassObjectRecord, renderSettings: RenderSettings) : RecordedObjectView() {

    init {
        if (renderSettings.showTypes()) {
            children.add(of(Class::class.java.name + ": ", Style.CALL_TREE_TYPE_NAME))
        }
        val text: Text = TrimmedTextView("class " + record.carriedType.name)
        children.add(text)
    }
}