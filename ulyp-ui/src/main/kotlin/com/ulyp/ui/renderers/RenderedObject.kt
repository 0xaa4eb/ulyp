package com.ulyp.ui.renderers

import com.ulyp.core.Type
import com.ulyp.core.recorders.*
import com.ulyp.ui.RenderSettings
import javafx.scene.Node
import javafx.scene.text.TextFlow
import java.util.function.Consumer

abstract class RenderedObject protected constructor(private val type: Type?) : TextFlow() {
    companion object {
        @JvmStatic
        fun of(repr: ObjectRecord, renderSettings: RenderSettings?): RenderedObject {
            val objectValue = when (repr) {
                is StringObjectRecord -> RenderedStringObject(repr, repr.getType(), renderSettings)
                is NullObjectRecord -> RenderedNull(renderSettings)
                is NotRecordedObjectRecord -> RenderedNotRecordedObject(renderSettings!!)
                is NumberRecord -> RenderedNumber(repr, repr.getType(), renderSettings!!)
                is ObjectArrayRecord -> RenderedObjectArray(repr, renderSettings)
                is CollectionRecord -> RenderedCollection(repr, renderSettings!!)
                is MapEntryRecord -> RenderedMapEntry(repr, renderSettings)
                is ClassObjectRecord -> RenderedClassObject(repr, renderSettings!!)
                is MapRecord -> RenderedMap(repr, renderSettings!!)
                is IdentityObjectRecord -> RenderedIdentityObject(repr, renderSettings!!)
                is ThrowableRecord -> RenderedThrowable(repr, renderSettings!!)
                is EnumRecord -> RenderedEnum(repr, renderSettings!!)
                is ToStringPrintedRecord -> RenderedToStringPrinted(repr, renderSettings!!)
                is DateRecord -> RenderDate(repr, renderSettings!!)
                is BooleanRecord -> RenderedBoolean(repr, renderSettings!!)
                else -> throw RuntimeException("Not supported for rendering: $repr")
            }
            objectValue.children.forEach(Consumer { node: Node ->
                node.styleClass.add("ulyp-ctt")
            })
            return objectValue
        }
    }
}