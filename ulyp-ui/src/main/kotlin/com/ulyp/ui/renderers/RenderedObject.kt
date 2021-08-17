package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.core.printers.*
import com.ulyp.ui.RenderSettings
import javafx.scene.Node
import javafx.scene.text.TextFlow
import java.util.function.Consumer

abstract class RenderedObject protected constructor(private val type: Type?) : TextFlow() {
    companion object {
        @JvmStatic
        fun of(repr: ObjectRepresentation, renderSettings: RenderSettings?): RenderedObject {
            val objectValue = when (repr) {
                is StringObjectRepresentation -> RenderedStringObject(repr, repr.getType(), renderSettings)
                is NullObjectRepresentation -> RenderedNull(renderSettings)
                is NotRecordedObjectRepresentation -> RenderedNotRecordedObject(renderSettings!!)
                is NumberObjectRepresentation -> RenderedNumber(repr, repr.getType(), renderSettings!!)
                is ObjectArrayRepresentation -> RenderedObjectArray(repr, renderSettings)
                is CollectionRepresentation -> RenderedCollection(repr, renderSettings!!)
                is MapEntryRepresentation -> RenderedMapEntry(repr, renderSettings)
                is ClassObjectRepresentation -> RenderedClassObject(repr, renderSettings!!)
                is MapRepresentation -> RenderedMap(repr, renderSettings!!)
                is IdentityObjectRepresentation -> RenderedIdentityObject(repr, renderSettings!!)
                is ThrowableRepresentation -> RenderedThrowable(repr, renderSettings!!)
                is EnumRepresentation -> RenderedEnum(repr, renderSettings!!)
                is ToStringPrintedRepresentation -> RenderedToStringPrinted(repr, renderSettings!!)
                is DateRepresentation -> RenderDate(repr, renderSettings!!)
                is BooleanRepresentation -> RenderedBoolean(repr, renderSettings!!)
                else -> throw RuntimeException("Not supported for rendering: $repr")
            }
            objectValue.children.forEach(Consumer { node: Node ->
                node.styleClass.add("ulyp-ctt")
                node.styleClass.add("ulyp-ctt-object-repr")
            })
            return objectValue
        }
    }
}