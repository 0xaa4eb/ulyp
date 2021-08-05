package com.ulyp.ui;

import com.ulyp.core.CallRecord;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

// TODO should be a better name
public class CallTreeNodeContent extends StackPane {

    public CallTreeNodeContent(CallRecord node, RenderSettings renderSettings, long totalNodeCountInTree) {

        setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(new CallRecordTreeNodeRelativeWeight(node, totalNodeCountInTree), new RenderedCallRecord(node, renderSettings));
    }
}
