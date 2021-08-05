package com.ulyp.ui.renderers;

import com.ulyp.core.printers.NumberObjectRepresentation;
import com.ulyp.core.Type;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;

import static com.ulyp.ui.util.CssClass.*;

public class RenderedNumber extends RenderedObject {

    protected RenderedNumber(NumberObjectRepresentation numberObjectRepresentation, Type type, RenderSettings renderSettings) {
        super(type);

        if (renderSettings.showTypes()) {
            super.getChildren().add(StyledText.of(type.getName(), CALL_TREE_TYPE_NAME));
            super.getChildren().add(StyledText.of(": ", CALL_TREE_PLAIN_TEXT));
        }
        super.getChildren().add(StyledText.of(numberObjectRepresentation.getNumberPrintedText(), CALL_TREE_NUMBER));
    }
}
