package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.arrays.ObjectArrayRecord
import com.ulyp.core.recorders.ObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedObjectArray(record: ObjectArrayRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {

        val nodes: MutableList<Node> = ArrayList()
        nodes.add(of("[", Style.CALL_TREE_COLLECTION_BRACKET))

        val recordedObjects = record.recordedItems
                .stream()
                .map { record: ObjectRecord -> of(record, renderSettings) }
                .collect(Collectors.toList())

        for (i in recordedObjects.indices) {

            nodes.add(recordedObjects[i])

            if (i != recordedObjects.size - 1 || recordedObjects.size < record.length) {
                nodes.add(of(", ", Style.CALL_TREE_NODE_SEPARATOR))
            }
        }
        if (recordedObjects.size < record.length) {
            nodes.add(
                    of(
                            (record.length - recordedObjects.size).toString() + " more...",
                            Style.CALL_TREE_NODE_SEPARATOR
                    )
            )
        }
        nodes.add(of("]", Style.CALL_TREE_COLLECTION_BRACKET))
        children.addAll(nodes)
    }
}