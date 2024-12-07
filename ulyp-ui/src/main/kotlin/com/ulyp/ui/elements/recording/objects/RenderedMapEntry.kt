package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.collections.MapEntryRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedMapEntry(record: MapEntryRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val nodes: MutableList<Node> = ArrayList()

        nodes += of(record.key, renderSettings)
        nodes += of(" -> ", Style.CALL_TREE_NODE_SEPARATOR)
        nodes += of(record.value, renderSettings)

        children.addAll(nodes)
    }
}