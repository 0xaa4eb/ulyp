package com.ulyp.ui.renderers

import com.ulyp.core.printers.NullObjectRecord
import com.ulyp.core.printers.ThrowableRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedThrowable(representation: ThrowableRecord, renderSettings: RenderSettings) : RenderedObject(representation.type) {

    init {
        val className =
            if (renderSettings.showTypes()) representation.type.name else toSimpleName(representation.type.name)
        val children: MutableList<Node> = ArrayList()
        children.add(of(className, CssClass.CALL_TREE_TYPE_NAME))
        children.add(of("(", CssClass.CALL_TREE_IDENTITY_REPR))
        if (representation.message !is NullObjectRecord) {
            children.add(of(representation.message, renderSettings))
        }
        children.add(of(")", CssClass.CALL_TREE_IDENTITY_REPR))
        super.getChildren().addAll(children)
    }
}