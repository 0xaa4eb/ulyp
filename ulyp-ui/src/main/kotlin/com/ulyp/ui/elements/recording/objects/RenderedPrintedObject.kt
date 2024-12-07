package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.PrintedObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedPrintedObject(record: PrintedObjectRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val nodes: MutableList<Node> = ArrayList()

        if (renderSettings.showTypes) {
            nodes += of(record.type.name, Style.CALL_TREE_TYPE_NAME)
            nodes += of("@", Style.CALL_TREE_IDENTITY)
            nodes += of(Integer.toHexString(record.identityHashCode), Style.CALL_TREE_IDENTITY)
            nodes += of(": ", Style.CALL_TREE_IDENTITY)
        }
        nodes += of(record.printedObject, Style.CALL_TREE_PRINTED, Style.CALL_TREE_BOLD)

        children.addAll(nodes)
    }
}