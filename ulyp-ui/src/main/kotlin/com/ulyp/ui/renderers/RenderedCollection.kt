package com.ulyp.ui.renderers

import com.ulyp.core.recorders.CollectionRecord
import com.ulyp.core.recorders.ObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedCollection(record: CollectionRecord, renderSettings: RenderSettings) : RenderedObject(record.type) {

    init {
        val renderedObjects = record.recordedItems
            .stream()
            .map { record: ObjectRecord -> of(record, renderSettings) }
            .collect(Collectors.toList())
        val texts: MutableList<Node> = ArrayList()
        if (renderSettings.showTypes()) {
            texts.add(of(record.type.name, CssClass.CALL_TREE_TYPE_NAME))
            texts.add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        texts.add(of("{", CssClass.CALL_TREE_COLLECTION_BRACKET))
        for (i in renderedObjects.indices) {
            texts.add(renderedObjects[i])
            if (i != renderedObjects.size - 1 || renderedObjects.size < record.length) {
                texts.add(of(", ", CssClass.CALL_TREE_NODE_SEPARATOR))
            }
        }
        if (renderedObjects.size < record.length) {
            texts.add(
                of(
                    (record.length - renderedObjects.size).toString() + " more...",
                    CssClass.CALL_TREE_NODE_SEPARATOR
                )
            )
        }
        texts.add(of("}", CssClass.CALL_TREE_COLLECTION_BRACKET))
        super.getChildren().addAll(texts)
    }
}