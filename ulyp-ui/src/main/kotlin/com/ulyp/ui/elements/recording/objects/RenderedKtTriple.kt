package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.kotlin.KtTripleRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedKtTriple(record: KtTripleRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val nodes: MutableList<Node> = ArrayList()

        if (renderSettings.showTypes) {
            nodes.add(of(record.type.name, Style.CALL_TREE_TYPE_NAME))
            nodes.add(of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }

        nodes.add(of("<", Style.CALL_TREE_COLLECTION_BRACKET))
        nodes.add(of(record.first, renderSettings))
        nodes.add(of(", ", Style.CALL_TREE_NODE_SEPARATOR))
        nodes.add(of(record.second, renderSettings))
        nodes.add(of(", ", Style.CALL_TREE_NODE_SEPARATOR))
        nodes.add(of(record.third, renderSettings))
        nodes.add(of(">", Style.CALL_TREE_COLLECTION_BRACKET))

        children.addAll(nodes)
    }
}