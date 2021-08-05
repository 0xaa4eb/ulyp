package com.ulyp.ui.renderers;

import com.ulyp.core.printers.ClassObjectRepresentation;
import com.ulyp.ui.RenderSettings;
import com.ulyp.ui.util.StyledText;
import javafx.scene.text.Text;

import static com.ulyp.ui.util.CssClass.CALL_TREE_TYPE_NAME;

public class RenderedClassObject extends RenderedObject {
    public RenderedClassObject(ClassObjectRepresentation classObject, RenderSettings renderSettings) {
        super(classObject.getType());

        if (renderSettings.showTypes()) {
            super.getChildren().add(StyledText.of(Class.class.getName() + ": ", CALL_TREE_TYPE_NAME));
        }

        Text text = new MultilinedText("class " + classObject.getCarriedType().getName());
        super.getChildren().add(text);
    }
}
