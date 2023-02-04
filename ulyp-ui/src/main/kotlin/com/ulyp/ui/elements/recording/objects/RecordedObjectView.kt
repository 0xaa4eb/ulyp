package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.*
import com.ulyp.core.recorders.arrays.ByteArrayRecord
import com.ulyp.core.recorders.arrays.ObjectArrayRecord
import com.ulyp.core.recorders.collections.CollectionRecord
import com.ulyp.core.recorders.collections.MapEntryRecord
import com.ulyp.core.recorders.collections.MapRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import javafx.scene.Node
import javafx.scene.text.TextFlow
import java.util.function.Consumer

abstract class RecordedObjectView protected constructor() : TextFlow() {
    companion object {
        @JvmStatic
        fun of(record: ObjectRecord, renderSettings: RenderSettings): RecordedObjectView {
            val objectValue = when (record) {
                is StringObjectRecord -> RecordedStringView(record)
                is CharObjectRecord -> RecordedCharView(record)
                is NullObjectRecord -> RecordedNullView()
                is NotRecordedObjectRecord -> NotRecordedObject(renderSettings)
                is NumberRecord -> RecordedNumberView(record.numberPrintedText, record.getType(), renderSettings)
                is FileRecord -> RecordedFileView(record.path, record.getType(), renderSettings)
                is ObjectArrayRecord -> RecordedObjectArrayView(record, renderSettings)
                is ByteArrayRecord -> RecordedByteArrayView(record)
                is CollectionRecord -> RecordedCollectionView(record, renderSettings)
                is MapEntryRecord -> RecordedMapEntry(record, renderSettings)
                is ClassObjectRecord -> RecordedClassObjectView(record, renderSettings)
                is MapRecord -> RecordedMapView(record, renderSettings)
                is IdentityObjectRecord -> RecordedIdentityObjectView(record, renderSettings)
                is ThrowableRecord -> RecordedExceptionView(record, renderSettings)
                is EnumRecord -> RecordedEnumView(record, renderSettings)
                is PrintedObjectRecord -> RecordedPrintedObjectView(record, renderSettings)
                is DateRecord -> RecordedDateView(record, renderSettings)
                is BooleanRecord -> RecordedBooleanView(record, renderSettings)
                is OptionalRecord -> RecordedOptionalView(record, renderSettings)
                else -> throw RuntimeException("Not supported for rendering: $record")
            }
            objectValue.children.forEach(Consumer { node: Node ->
                node.styleClass.addAll(Style.CALL_TREE.cssClasses)
            })
            return objectValue
        }
    }
}