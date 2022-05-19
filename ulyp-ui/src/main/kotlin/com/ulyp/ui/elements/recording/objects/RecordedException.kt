package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.NullObjectRecord
import com.ulyp.core.recorders.ThrowableRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RecordedException(record: ThrowableRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        val className =
            if (renderSettings.showTypes()) record.type.name else toSimpleName(record.type.name)
        val children: MutableList<Node> = ArrayList()
        children.add(of(className, Style.CALL_TREE_TYPE_NAME))
        children.add(of("(", Style.CALL_TREE_IDENTITY))
        if (record.message !is NullObjectRecord) {
            children.add(of(record.message, renderSettings))
        }
        children.add(of(")", Style.CALL_TREE_IDENTITY))
        super.getChildren().addAll(children)
    }
}