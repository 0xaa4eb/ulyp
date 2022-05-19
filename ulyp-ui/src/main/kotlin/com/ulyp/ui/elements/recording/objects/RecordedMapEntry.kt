package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.MapEntryRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RecordedMapEntry(record: MapEntryRecord, renderSettings: RenderSettings?) : RecordedObject(record.type) {

    init {
        val texts: MutableList<Node> = ArrayList()
        texts.add(of(record.key, renderSettings))
        texts.add(of(" -> ", CssClass.CALL_TREE_NODE_SEPARATOR_CSS))
        texts.add(of(record.value, renderSettings))
        super.getChildren().addAll(texts)
    }
}