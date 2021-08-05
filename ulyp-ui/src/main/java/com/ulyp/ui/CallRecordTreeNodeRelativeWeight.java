package com.ulyp.ui;

import com.ulyp.core.CallRecord;
import javafx.scene.layout.Region;

/**
 * A background rectangle which shows how much nested calls every call record tree node has.
 */
public class CallRecordTreeNodeRelativeWeight extends Region {

    public CallRecordTreeNodeRelativeWeight(CallRecord node, long totalNodeCountInTree) {
        int width = (int) (600.0 * node.getSubtreeNodeCount() / totalNodeCountInTree);

        // TODO move this to CSS
        setStyle(
                "-fx-background-color: black; " +
                        "-fx-border-style: solid; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-color: rgb(50, 50, 50); " +
                        String.format("-fx-min-width: %d; ", width) +
                        "-fx-min-height: 20; " +
                        String.format("-fx-max-width: %d; ", width) +
                        "-fx-max-height: 20;"
        );
    }
}
