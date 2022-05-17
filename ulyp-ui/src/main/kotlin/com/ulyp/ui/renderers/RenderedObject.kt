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
        fun of(record: ObjectRecord, renderSettings: RenderSettings?): RenderedObject {
            val objectValue = when (record) {
                is StringObjectRecord -> RenderedString(record, record.getType(), renderSettings)
                is CharObjectRecord -> RenderedChar(record, record.getType(), renderSettings!!)
                is NullObjectRecord -> RenderedNull(renderSettings)
                is NotRecordedObjectRecord -> RenderedNotRecordedObject(renderSettings!!)
                is NumberRecord -> RenderedNumber(record.numberPrintedText, record.getType(), renderSettings!!)
                is FileRecord -> RenderedFile(record.path, record.getType(), renderSettings!!)
                is ObjectArrayRecord -> RenderedObjectArray(record, renderSettings)
                is CollectionRecord -> RenderedCollection(record, renderSettings!!)
                is MapEntryRecord -> RenderedMapEntry(record, renderSettings)
                is ClassObjectRecord -> RenderedClassObject(record, renderSettings!!)
                is MapRecord -> RenderedMap(record, renderSettings!!)
                is IdentityObjectRecord -> RenderedIdentityObject(record, renderSettings!!)
                is ThrowableRecord -> RenderedThrowable(record, renderSettings!!)
                is EnumRecord -> RenderedEnum(record, renderSettings!!)
                is PrintedObjectRecord -> RenderedPrintedObject(record, renderSettings!!)
                is DateRecord -> RenderDate(record, renderSettings!!)
                is BooleanRecord -> RenderedBoolean(record, renderSettings!!)
                is OptionalRecord -> RenderedOptional(record, renderSettings!!)
                else -> throw RuntimeException("Not supported for rendering: $record")
            }
            objectValue.children.forEach(Consumer { node: Node ->
                node.styleClass.add("ulyp-ctt")
            })
            return objectValue
        }
    }
}