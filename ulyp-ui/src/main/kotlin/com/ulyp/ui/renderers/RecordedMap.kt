package com.ulyp.ui.renderers

import com.ulyp.core.recorders.MapEntryRecord
import com.ulyp.core.recorders.MapRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RecordedMap(record: MapRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        val entries = record.entries
            .stream()
            .map { record: MapEntryRecord -> of(record, renderSettings) }
            .collect(Collectors.toList())
        val texts: MutableList<Node> = ArrayList()
        if (renderSettings.showTypes()) {
            texts.add(of(record.type.name, CssClass.CALL_TREE_TYPE_NAME))
            texts.add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        texts.add(of("{", CssClass.CALL_TREE_COLLECTION_BRACKET))
        for (i in entries.indices) {
            texts.add(entries[i])
            if (i != entries.size - 1 || entries.size < record.size) {
                texts.add(of(", ", CssClass.CALL_TREE_NODE_SEPARATOR))
            }
        }
        if (entries.size < record.size) {
            texts.add(of((record.size - entries.size).toString() + " more...", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        texts.add(of("}", CssClass.CALL_TREE_COLLECTION_BRACKET))
        super.getChildren().addAll(texts)
    }
}