package com.ulyp.ui.renderers

import com.ulyp.core.printers.MapEntryRepresentation
import com.ulyp.core.printers.MapRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedMap(representation: MapRepresentation, renderSettings: RenderSettings) : RenderedObject(representation.type) {

    init {
        val entries = representation.entries
            .stream()
            .map { repr: MapEntryRepresentation -> of(repr, renderSettings) }
            .collect(Collectors.toList())
        val texts: MutableList<Node> = ArrayList()
        if (renderSettings.showTypes()) {
            texts.add(of(representation.type.name, CssClass.CALL_TREE_TYPE_NAME))
            texts.add(of(": ", CssClass.CALL_TREE_PLAIN_TEXT))
        }
        texts.add(of("{", CssClass.CALL_TREE_COLLECTION_BRACE))
        for (i in entries.indices) {
            texts.add(entries[i])
            if (i != entries.size - 1 || entries.size < representation.size) {
                texts.add(of(", ", CssClass.CALL_TREE_PLAIN_TEXT))
            }
        }
        if (entries.size < representation.size) {
            texts.add(of((representation.size - entries.size).toString() + " more...", CssClass.CALL_TREE_PLAIN_TEXT))
        }
        texts.add(of("}", CssClass.CALL_TREE_COLLECTION_BRACE))
        super.getChildren().addAll(texts)
    }
}