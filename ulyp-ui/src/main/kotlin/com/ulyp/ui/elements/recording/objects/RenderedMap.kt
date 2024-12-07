package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.collections.MapEntryRecord
import com.ulyp.core.recorders.collections.MapRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node
import java.util.stream.Collectors

class RenderedMap(record: MapRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val entries = record.entries
                .stream()
                .map { record: MapEntryRecord -> of(record, renderSettings) }
                .collect(Collectors.toList())
        val nodes: MutableList<Node> = ArrayList()
        
        if (renderSettings.showTypes) {
            nodes += of(record.type.name, Style.CALL_TREE_TYPE_NAME)
            nodes += of(": ", Style.CALL_TREE_NODE_SEPARATOR)
        }
        nodes += of("{", Style.CALL_TREE_COLLECTION_BRACKET)
        for (i in entries.indices) {
            nodes += entries[i]
            if (i != entries.size - 1 || entries.size < record.size) {
                nodes += of(", ", Style.CALL_TREE_NODE_SEPARATOR)
            }
        }
        if (entries.size < record.size) {
            nodes += of((record.size - entries.size).toString() + " more...", Style.CALL_TREE_NODE_SEPARATOR)
        }
        nodes += of("}", Style.CALL_TREE_COLLECTION_BRACKET)

        children.addAll(nodes)
    }
}