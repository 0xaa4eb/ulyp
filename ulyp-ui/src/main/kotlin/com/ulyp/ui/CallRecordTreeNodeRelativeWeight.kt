package com.ulyp.ui

import com.ulyp.core.CallRecord
import javafx.scene.layout.Region

/**
 * A background rectangle which shows how much nested calls every call record tree node has.
 */
class CallRecordTreeNodeRelativeWeight(node: CallRecord, totalNodeCountInTree: Long) : Region() {
    init {
        val width = (600.0 * node.subtreeNodeCount / totalNodeCountInTree).toInt()

        // TODO move this to CSS
        style = "-fx-background-color: black; " +
                "-fx-border-style: solid; " +
                "-fx-border-width: 2; " +
                "-fx-border-color: rgb(50, 50, 50); " + String.format("-fx-min-width: %d; ", width) +
                "-fx-min-height: 20; " + String.format("-fx-max-width: %d; ", width) +
                "-fx-max-height: 20;"
    }
}