package com.ulyp.ui.renderers;

import com.ulyp.core.printers.DateRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;

import static com.ulyp.ui.util.CssClass.*;

public class RenderDate extends RenderedObject {
    public RenderDate(DateRepresentation repr, RenderSettings renderSettings) {
        super(repr.getType());

        if (renderSettings.showTypes()) {
            super.getChildren().add(StyledText.of(repr.getType().getName(), CALL_TREE_TYPE_NAME));
            super.getChildren().add(StyledText.of(": ", CALL_TREE_PLAIN_TEXT));
        }
        super.getChildren().add(StyledText.of(repr.getDatePrinted(), CALL_TREE_NUMBER));
    }
}
