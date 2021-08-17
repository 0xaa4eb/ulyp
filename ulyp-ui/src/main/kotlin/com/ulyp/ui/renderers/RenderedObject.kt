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
            val objectValue: RenderedObject

            // TODO replace with map
            objectValue = if (repr is StringObjectRepresentation) {
                RenderedStringObject(repr, repr.getType(), renderSettings)
            } else if (repr is NullObjectRepresentation) {
                RenderedNull(renderSettings)
            } else if (repr is NotRecordedObjectRepresentation) {
                RenderedNotRecordedObject(renderSettings!!)
            } else if (repr is NumberObjectRepresentation) {
                RenderedNumber(repr, repr.getType(), renderSettings!!)
            } else if (repr is ObjectArrayRepresentation) {
                RenderedObjectArray(repr, renderSettings)
            } else if (repr is CollectionRepresentation) {
                RenderedCollection(repr, renderSettings!!)
            } else if (repr is MapEntryRepresentation) {
                RenderedMapEntry(repr, renderSettings)
            } else if (repr is ClassObjectRepresentation) {
                RenderedClassObject(repr, renderSettings!!)
            } else if (repr is MapRepresentation) {
                RenderedMap(repr, renderSettings!!)
            } else if (repr is IdentityObjectRepresentation) {
                RenderedIdentityObject(repr, renderSettings!!)
            } else if (repr is ThrowableRepresentation) {
                RenderedThrowable(repr, renderSettings!!)
            } else if (repr is EnumRepresentation) {
                RenderedEnum(repr, renderSettings!!)
            } else if (repr is ToStringPrintedRepresentation) {
                RenderedToStringPrinted(repr, renderSettings!!)
            } else if (repr is DateRepresentation) {
                RenderDate(repr, renderSettings!!)
            } else if (repr is BooleanRepresentation) {
                RenderedBoolean(repr, renderSettings!!)
            } else {
                throw RuntimeException("Not supported for rendering: $repr")
            }
            objectValue.children.forEach(Consumer { node: Node ->
                node.styleClass.add("ulyp-ctt")
                node.styleClass.add("ulyp-ctt-object-repr")
            })
            return objectValue
        }
    }
}