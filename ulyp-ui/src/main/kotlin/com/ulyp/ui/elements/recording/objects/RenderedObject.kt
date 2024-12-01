package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.*
import com.ulyp.core.recorders.arrays.ByteArrayRecord
import com.ulyp.core.recorders.arrays.ArrayRecord
import com.ulyp.core.recorders.basic.*
import com.ulyp.core.recorders.collections.CollectionRecord
import com.ulyp.core.recorders.collections.MapEntryRecord
import com.ulyp.core.recorders.collections.MapRecord
import com.ulyp.core.recorders.kotlin.KtPairRecord
import com.ulyp.core.recorders.numeric.IntegralRecord
import com.ulyp.core.recorders.numeric.NumberRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import javafx.scene.Node
import javafx.scene.text.TextFlow
import java.util.function.Consumer

abstract class RenderedObject protected constructor() : TextFlow() {
    companion object {
        @JvmStatic
        fun of(record: ObjectRecord, renderSettings: RenderSettings): RenderedObject {
            val objectValue = when (record) {
                is StringObjectRecord -> RenderedString(record)
                is CharRecord -> RenderedChar(record)
                is NullObjectRecord -> RenderedNull()
                is NotRecordedObjectRecord -> NotRecordedRenderedObject(renderSettings)
                is NumberRecord -> RenderedNumber(record.numberPrintedText, record.getType(), renderSettings)
                is FileRecord -> RenderedFile(record.path, record.getType(), renderSettings)
                is ArrayRecord -> RenderedArray(record, renderSettings)
                is ByteArrayRecord -> RenderedByteArray(record)
                is CollectionRecord -> RenderedCollection(record, renderSettings)
                is MapEntryRecord -> RenderedMapEntry(record, renderSettings)
                is ClassRecord -> RenderedClass(record, renderSettings)
                is MethodRecord -> RenderedMethod(record, renderSettings)
                is MapRecord -> RenderedMap(record, renderSettings)
                is IdentityObjectRecord -> RenderedIdentityObject(record, renderSettings)
                is ThrowableRecord -> RenderedException(record, renderSettings)
                is EnumRecord -> RenderedEnum(record, renderSettings)
                is PrintedObjectRecord -> RenderedPrintedObject(record, renderSettings)
                is DateRecord -> RenderedDate(record, renderSettings)
                is BooleanRecord -> RenderedBoolean(record, renderSettings)
                is OptionalRecord -> RenderedOptional(record, renderSettings)
                is IntegralRecord -> RenderedNumber(record.value.toString(), record.type, renderSettings)
                is KtPairRecord -> RenderedKtPair(record, renderSettings)
                else -> throw RuntimeException("Not supported for rendering: $record")
            }
            objectValue.children.forEach(Consumer { node: Node ->
                node.styleClass.addAll(Style.CALL_TREE.cssClasses)
            })
            return objectValue
        }
    }
}