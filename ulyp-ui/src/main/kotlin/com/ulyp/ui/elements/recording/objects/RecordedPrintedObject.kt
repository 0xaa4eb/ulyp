package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.PrintedObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RecordedPrintedObject(record: PrintedObjectRecord, renderSettings: RenderSettings) : RecordedObject() {

    init {
        val typeName =
            if (renderSettings.showTypes()) record.type.name else toSimpleName(record.type.name)

        val nodes: MutableList<Node> = ArrayList()


        if (record.printed.value().contains(typeName)) {
            nodes.add(of(typeName, Style.CALL_TREE_TYPE_NAME))
            nodes.add(of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }
        nodes.add(of(record.printed, renderSettings))
        nodes.add(of("@", Style.CALL_TREE_IDENTITY))
        nodes.add(of(Integer.toHexString(record.identityHashCode), Style.CALL_TREE_IDENTITY))

        children.addAll(nodes)
    }
}