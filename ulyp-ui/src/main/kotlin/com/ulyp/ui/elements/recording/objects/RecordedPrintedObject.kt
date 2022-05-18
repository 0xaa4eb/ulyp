package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.PrintedObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RecordedPrintedObject(record: PrintedObjectRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        val typeName =
            if (renderSettings.showTypes()) record.type.name else toSimpleName(record.type.name)

        val nodes: MutableList<Node> = ArrayList()


        if (record.printed.value().contains(typeName)) {
            nodes.add(of(typeName, CssClass.CALL_TREE_TYPE_NAME))
            nodes.add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        nodes.add(of(record.printed, renderSettings))
        nodes.add(of("@", CssClass.CALL_TREE_IDENTITY))
        nodes.add(of(Integer.toHexString(record.identityHashCode), CssClass.CALL_TREE_IDENTITY))

        super.getChildren().addAll(nodes)
    }
}