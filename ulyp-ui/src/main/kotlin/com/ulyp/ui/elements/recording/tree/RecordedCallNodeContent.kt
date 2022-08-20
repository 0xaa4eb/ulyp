package com.ulyp.ui.elements.recording.tree

import com.ulyp.storage.CallRecord
import com.ulyp.ui.RenderSettings
import javafx.geometry.Pos
import javafx.scene.layout.StackPane


class RecordedCallNodeContent(node: CallRecord, renderSettings: RenderSettings, totalNodeCountInTree: Int) : StackPane() {

    init {
        alignment = Pos.CENTER_LEFT
        children.addAll(
                RecordedCallWeight(node, totalNodeCountInTree),
                RecordedCallView(node, renderSettings)
        )
    }
}