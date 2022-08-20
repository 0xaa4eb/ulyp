package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.OptionalRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RecordedOptionalView(record: OptionalRecord, renderSettings: RenderSettings) : RecordedObjectView() {

    init {
        val content: MutableList<Node> = ArrayList()
        if (renderSettings.showTypes()) {
            content.add(of(record.type.name, Style.CALL_TREE_TYPE_NAME))
            content.add(of(": ", Style.CALL_TREE_NODE_SEPARATOR))
        }
        content.add(of("Optional", Style.CALL_TREE_NODE_SEPARATOR))
        content.add(of("<", Style.CALL_TREE_COLLECTION_BRACKET))
        if (!record.isEmpty) {
            content.add(of(record.value, renderSettings))
        }
        content.add(of(">", Style.CALL_TREE_COLLECTION_BRACKET))
        children.addAll(content)
    }
}