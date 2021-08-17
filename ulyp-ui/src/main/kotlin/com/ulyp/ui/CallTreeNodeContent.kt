package com.ulyp.ui

import com.ulyp.core.CallRecord
import javafx.geometry.Pos
import javafx.scene.layout.StackPane

// TODO should be a better name
class CallTreeNodeContent(node: CallRecord, renderSettings: RenderSettings?, totalNodeCountInTree: Long) :
    StackPane() {
    init {
        alignment = Pos.CENTER_LEFT
        children.addAll(
            CallRecordTreeNodeRelativeWeight(node, totalNodeCountInTree),
            RenderedCallRecord(node, renderSettings!!)
        )
    }
}