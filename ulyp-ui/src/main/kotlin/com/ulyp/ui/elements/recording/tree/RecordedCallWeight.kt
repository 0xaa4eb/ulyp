package com.ulyp.ui.elements.recording.tree

import com.ulyp.storage.tree.CallRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.settings.RecordedCallWeightType
import javafx.scene.layout.Region
import java.time.Duration

/**
 * Every call in a call tree has weight. It's drawn as a rectangle in a background.
 * It represents how much time/calls current call's subtree has in comparison to other calls. Currently, weight can
 * be either time spent or call count.
 */
class RecordedCallWeight(renderSettings: RenderSettings, node: CallRecord, totalNodeCountInTree: Int, rootDuration: Duration) : Region() {
    init {
        // TODO move 600.0 to settings

        val width: Int = when(renderSettings.recordedCallWeightType) {
            RecordedCallWeightType.TIME ->
                if (rootDuration.nano > 0) {
                    (600.0 * node.nanosDuration / rootDuration.nano).toInt()
                } else {
                    // timestamps not enabled, do not draw rectangle
                    0
                }
            RecordedCallWeightType.CALLS ->
                (600.0 * node.subtreeSize / totalNodeCountInTree).toInt()
        }


        styleClass += "ulyp-call-tree-call-node"

        style = String.format("-fx-min-width: %d; ", width) +
                "\n" +
                String.format("-fx-max-width: %d; ", width)
    }
}