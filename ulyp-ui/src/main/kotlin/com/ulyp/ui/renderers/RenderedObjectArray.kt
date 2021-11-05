package com.ulyp.ui.renderers

import com.ulyp.core.printers.ObjectArrayRecord
import com.ulyp.core.printers.ObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedObjectArray(arrayRepresentation: ObjectArrayRecord, renderSettings: RenderSettings?) : RenderedObject(arrayRepresentation.type) {

    init {
        val renderedObjects = arrayRepresentation.recordedItems
            .stream()
            .map { repr: ObjectRecord -> of(repr, renderSettings) }
            .collect(Collectors.toList())
        val texts: MutableList<Node> = ArrayList()
        texts.add(of("[", CssClass.CALL_TREE_COLLECTION_BRACKET))
        for (i in renderedObjects.indices) {
            texts.add(renderedObjects[i])
            if (i != renderedObjects.size - 1 || renderedObjects.size < arrayRepresentation.length) {
                texts.add(of(", ", CssClass.CALL_TREE_NODE_SEPARATOR))
            }
        }
        if (renderedObjects.size < arrayRepresentation.length) {
            texts.add(
                of(
                    (arrayRepresentation.length - renderedObjects.size).toString() + " more...",
                    CssClass.CALL_TREE_NODE_SEPARATOR
                )
            )
        }
        texts.add(of("]", CssClass.CALL_TREE_COLLECTION_BRACKET))
        super.getChildren().addAll(texts)
    }
}