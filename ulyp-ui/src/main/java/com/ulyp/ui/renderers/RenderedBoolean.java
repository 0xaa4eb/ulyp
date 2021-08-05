package com.ulyp.ui.renderers;

import com.ulyp.core.printers.BooleanRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;

import static com.ulyp.ui.util.CssClass.*;

public class RenderedBoolean extends RenderedObject {

    protected RenderedBoolean(BooleanRepresentation representation, RenderSettings renderSettings) {
        super(representation.getType());

        if (renderSettings.showTypes()) {
            super.getChildren().add(StyledText.of(representation.getType().getName(), CALL_TREE_TYPE_NAME));
            super.getChildren().add(StyledText.of(": ", CALL_TREE_PLAIN_TEXT));
        }
        super.getChildren().add(StyledText.of(String.valueOf(representation.value()), CALL_TREE_NUMBER));
    }
}
