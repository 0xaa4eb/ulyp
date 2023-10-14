package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.collections.MapEntryRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedMapEntry(record: MapEntryRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val texts: MutableList<Node> = ArrayList()
        texts.add(of(record.key, renderSettings))
        texts.add(of(" -> ", Style.CALL_TREE_NODE_SEPARATOR))
        texts.add(of(record.value, renderSettings))
        children.addAll(texts)
    }
}