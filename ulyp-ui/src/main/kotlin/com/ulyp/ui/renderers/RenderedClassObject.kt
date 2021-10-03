package com.ulyp.ui.renderers

import com.ulyp.core.printers.ClassObjectRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.text.Text

class RenderedClassObject(classObject: ClassObjectRepresentation, renderSettings: RenderSettings) : RenderedObject(classObject.type) {

    init {
        if (renderSettings.showTypes()) {
            super.getChildren().add(of(Class::class.java.name + ": ", CssClass.CALL_TREE_TYPE_NAME))
        }
        val text: Text = MultilinedText("class " + classObject.carriedType.name)
        super.getChildren().add(text)
    }
}