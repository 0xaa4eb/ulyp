package com.ulyp.ui.renderers

import com.ulyp.core.printers.ToStringPrintedRepresentation
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedToStringPrinted(representation: ToStringPrintedRepresentation, renderSettings: RenderSettings) : RenderedObject(representation.type) {

    init {
        val className =
            if (renderSettings.showTypes()) representation.type.name else toSimpleName(representation.type.name)
        val nodes: MutableList<Node> = ArrayList()
        nodes.add(of(className, CssClass.CALL_TREE_TYPE_NAME))
        nodes.add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        nodes.add(of(representation.printed, renderSettings))
        if (renderSettings.showTypes()) {
            nodes.add(of("@", CssClass.CALL_TREE_IDENTITY_REPR))
            nodes.add(of(Integer.toHexString(representation.identityHashCode), CssClass.CALL_TREE_IDENTITY_REPR))
        }
        super.getChildren().addAll(nodes)
    }
}