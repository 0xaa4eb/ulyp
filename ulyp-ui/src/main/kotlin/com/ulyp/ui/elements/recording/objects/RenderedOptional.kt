package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.OptionalRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedOptional(record: OptionalRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val nodes: MutableList<Node> = ArrayList()

        if (renderSettings.showTypes) {
            nodes += of(record.type.name, Style.CALL_TREE_TYPE_NAME)
            nodes += of(": ", Style.CALL_TREE_NODE_SEPARATOR)
        }
        nodes += of("Optional", Style.CALL_TREE_NODE_SEPARATOR)
        nodes += of("<", Style.CALL_TREE_COLLECTION_BRACKET)
        if (!record.isEmpty) {
            nodes += of(record.value, renderSettings)
        }
        nodes += of(">", Style.CALL_TREE_COLLECTION_BRACKET)

        children.addAll(nodes)
    }
}