package com.ulyp.ui

import com.ulyp.storage.CallRecord
import javafx.scene.layout.Region

/**
 * A background rectangle which shows how much nested calls every call record tree node has.
 */
class RecordingTreeNodeWeight(node: CallRecord, totalNodeCountInTree: Long) : Region() {
    init {
        val width = (600.0 * node.subtreeSize / totalNodeCountInTree).toInt()

        styleClass.add("ulyp-ctt-call-node")

        style = String.format("-fx-min-width: %d; ", width) +
                "\n" +
                String.format("-fx-max-width: %d; ", width)
    }
}