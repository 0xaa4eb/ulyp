package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.NullObjectRecord
import com.ulyp.core.recorders.basic.ThrowableRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedException(record: ThrowableRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val className =
                if (renderSettings.showTypes) record.type.name else toSimpleName(record.type.name)
        val childrenToAdd: MutableList<Node> = ArrayList()
        childrenToAdd.add(of(className, Style.CALL_TREE_TYPE_NAME))
        childrenToAdd.add(of("(", Style.CALL_TREE_IDENTITY))
        if (record.message !is NullObjectRecord) {
            childrenToAdd.add(of(record.message, renderSettings))
        }
        childrenToAdd.add(of(")", Style.CALL_TREE_IDENTITY))
        children.addAll(childrenToAdd)
    }
}