package com.ulyp.ui.renderers

import com.ulyp.core.printers.MapEntryRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedMapEntry(entry: MapEntryRecord, renderSettings: RenderSettings?) : RenderedObject(entry.type) {

    init {
        val texts: MutableList<Node> = ArrayList()
        texts.add(of(entry.key, renderSettings))
        texts.add(of(" -> ", CssClass.CALL_TREE_NODE_SEPARATOR))
        texts.add(of(entry.value, renderSettings))
        super.getChildren().addAll(texts)
    }
}