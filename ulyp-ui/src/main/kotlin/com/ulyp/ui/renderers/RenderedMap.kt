package com.ulyp.ui.renderers

import com.ulyp.core.printers.MapEntryRecord
import com.ulyp.core.printers.MapRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedMap(representation: MapRecord, renderSettings: RenderSettings) : RenderedObject(representation.type) {

    init {
        val entries = representation.entries
            .stream()
            .map { repr: MapEntryRecord -> of(repr, renderSettings) }
            .collect(Collectors.toList())
        val texts: MutableList<Node> = ArrayList()
        if (renderSettings.showTypes()) {
            texts.add(of(representation.type.name, CssClass.CALL_TREE_TYPE_NAME))
            texts.add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        texts.add(of("{", CssClass.CALL_TREE_COLLECTION_BRACKET))
        for (i in entries.indices) {
            texts.add(entries[i])
            if (i != entries.size - 1 || entries.size < representation.size) {
                texts.add(of(", ", CssClass.CALL_TREE_NODE_SEPARATOR))
            }
        }
        if (entries.size < representation.size) {
            texts.add(of((representation.size - entries.size).toString() + " more...", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        texts.add(of("}", CssClass.CALL_TREE_COLLECTION_BRACKET))
        super.getChildren().addAll(texts)
    }
}