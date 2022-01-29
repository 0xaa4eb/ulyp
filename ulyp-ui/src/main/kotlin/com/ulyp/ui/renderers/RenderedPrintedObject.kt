package com.ulyp.ui.renderers

import com.ulyp.core.recorders.PrintedObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedPrintedObject(representation: PrintedObjectRecord, renderSettings: RenderSettings) : RenderedObject(representation.type) {

    init {
        val typeName =
            if (renderSettings.showTypes()) representation.type.name else toSimpleName(representation.type.name)

        val nodes: MutableList<Node> = ArrayList()


        if (representation.printed.value().contains(typeName)) {
            nodes.add(of(typeName, CssClass.CALL_TREE_TYPE_NAME))
            nodes.add(of(": ", CssClass.CALL_TREE_NODE_SEPARATOR))
        }
        nodes.add(of(representation.printed, renderSettings))
        nodes.add(of("@", CssClass.CALL_TREE_IDENTITY_REPR))
        nodes.add(of(Integer.toHexString(representation.identityHashCode), CssClass.CALL_TREE_IDENTITY_REPR))

        super.getChildren().addAll(nodes)
    }
}