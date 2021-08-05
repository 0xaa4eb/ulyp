package com.ulyp.ui.renderers;

import com.ulyp.core.printers.StringObjectRepresentation;
import com.ulyp.core.Type;
import com.ulyp.ui.RenderSettings;
import javafx.scene.text.Text;

public class RenderedStringObject extends RenderedObject {

    RenderedStringObject(StringObjectRepresentation representation, Type classDescription, RenderSettings renderSettings) {
        super(classDescription);
        Text text = new MultilinedText("\"" + representation.value() + "\"");
        text.getStyleClass().add("ulyp-ctt-string");

        super.getChildren().add(text);
    }
}
