package com.ulyp.ui.renderers

import com.ulyp.core.printers.ObjectArrayRepresentation
import com.ulyp.core.printers.ObjectRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedObjectArray(arrayRepresentation: ObjectArrayRepresentation, renderSettings: RenderSettings?) : RenderedObject(arrayRepresentation.type) {

    init {
        val renderedObjects = arrayRepresentation.recordedItems
            .stream()
            .map { repr: ObjectRepresentation -> of(repr, renderSettings) }
            .collect(Collectors.toList())
        val texts: MutableList<Node> = ArrayList()
        texts.add(of("[", CssClass.CALL_TREE_COLLECTION_BRACE))
        for (i in renderedObjects.indices) {
            texts.add(renderedObjects[i])
            if (i != renderedObjects.size - 1 || renderedObjects.size < arrayRepresentation.length) {
                texts.add(of(", ", CssClass.CALL_TREE_PLAIN_TEXT))
            }
        }
        if (renderedObjects.size < arrayRepresentation.length) {
            texts.add(
                of(
                    (arrayRepresentation.length - renderedObjects.size).toString() + " more...",
                    CssClass.CALL_TREE_PLAIN_TEXT
                )
            )
        }
        texts.add(of("]", CssClass.CALL_TREE_COLLECTION_BRACE))
        super.getChildren().addAll(texts)
    }
}