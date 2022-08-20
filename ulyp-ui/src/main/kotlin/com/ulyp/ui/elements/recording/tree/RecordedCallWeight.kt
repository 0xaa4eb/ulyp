package com.ulyp.ui.elements.recording.tree

import com.ulyp.storage.CallRecord
import javafx.scene.layout.Region

/**
 * A background rectangle which approximately shows how many nested calls every call tree node has.
 */
class RecordedCallWeight(node: CallRecord, totalNodeCountInTree: Int) : Region() {
    init {
        val width = (600.0 * node.subtreeSize / totalNodeCountInTree).toInt()

        styleClass += "ulyp-call-tree-call-node"

        style = String.format("-fx-min-width: %d; ", width) +
                "\n" +
                String.format("-fx-max-width: %d; ", width)
    }
}