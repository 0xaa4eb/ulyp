package com.ulyp.ui.renderers;

import com.ulyp.core.Type;
import com.ulyp.ui.RenderSettings;
import javafx.scene.text.Text;

public class RenderedNull extends RenderedObject {

    RenderedNull(RenderSettings renderSettings) {
        super(Type.unknown());

        super.getChildren().add(new Text("null"));
    }
}
