package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.collections.CollectionRecord
import com.ulyp.core.recorders.ObjectRecord
import com.ulyp.core.recorders.collections.CollectionType
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedCollection(record: CollectionRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val recordedObjects = record.elements
                .stream()
                .map { record: ObjectRecord -> of(record, renderSettings) }
                .collect(Collectors.toList())

        val nodes: MutableList<Node> = ArrayList()

        if (renderSettings.showTypes) {
            nodes.add(of(record.type.name, Style.CALL_TREE_TYPE_NAME))
            nodes.add(of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }

        when (record.collectionType) {
            CollectionType.LIST -> nodes.add(of("[", Style.CALL_TREE_COLLECTION_BRACKET))
            CollectionType.SET -> nodes.add(of("{", Style.CALL_TREE_COLLECTION_BRACKET))
            CollectionType.QUEUE -> nodes.add(of("<", Style.CALL_TREE_COLLECTION_BRACKET))
            CollectionType.OTHER -> nodes.add(of("{", Style.CALL_TREE_COLLECTION_BRACKET))
            null -> nodes.add(of("{", Style.CALL_TREE_COLLECTION_BRACKET))
        }

        for (i in recordedObjects.indices) {

            nodes.add(recordedObjects[i])

            if (i != recordedObjects.size - 1 || recordedObjects.size < record.size) {
                nodes.add(of(", ", Style.CALL_TREE_NODE_SEPARATOR))
            }
        }
        if (recordedObjects.size < record.size) {
            nodes.add(
                    of(
                            (record.size - recordedObjects.size).toString() + " more...",
                            Style.CALL_TREE_NODE_SEPARATOR
                    )
            )
        }

        when (record.collectionType) {
            CollectionType.LIST -> nodes.add(of("]", Style.CALL_TREE_COLLECTION_BRACKET))
            CollectionType.SET -> nodes.add(of("}", Style.CALL_TREE_COLLECTION_BRACKET))
            CollectionType.QUEUE -> nodes.add(of(">", Style.CALL_TREE_COLLECTION_BRACKET))
            CollectionType.OTHER -> nodes.add(of("}", Style.CALL_TREE_COLLECTION_BRACKET))
            null -> nodes.add(of("}", Style.CALL_TREE_COLLECTION_BRACKET))
        }

        children.addAll(nodes)
    }
}