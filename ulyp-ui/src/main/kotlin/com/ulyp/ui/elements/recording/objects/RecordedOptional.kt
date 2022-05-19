package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.OptionalRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RecordedOptional(record: OptionalRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        val content: MutableList<Node> = ArrayList()
        if (renderSettings.showTypes()) {
            content.add(of(record.type.name, CssClass.CALL_TREE_TYPE_NAME_CSS))
            content.add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR_CSS))
        }
        content.add(of("Optional", CssClass.CALL_TREE_NODE_SEPARATOR_CSS))
        content.add(of("<", CssClass.CALL_TREE_COLLECTION_BRACKET_CSS))
        if (!record.isEmpty) {
            content.add(of(record.value, renderSettings))
        }
        content.add(of(">", CssClass.CALL_TREE_COLLECTION_BRACKET_CSS))
        super.getChildren().addAll(content)
    }
}