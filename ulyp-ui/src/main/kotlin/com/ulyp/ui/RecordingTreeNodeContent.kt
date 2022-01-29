package com.ulyp.ui

import com.ulyp.storage.CallRecord
import javafx.geometry.Pos
import javafx.scene.layout.StackPane

// TODO should be a better name
class RecordingTreeNodeContent(node: CallRecord, renderSettings: RenderSettings?, totalNodeCountInTree: Int) : StackPane() {

    init {
        alignment = Pos.CENTER_LEFT
        children.addAll(
            RecordingTreeNodeWeight(node, totalNodeCountInTree),
            RecordingTreeCall(node, renderSettings!!)
        )
    }
}