package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.*
import com.ulyp.ui.RenderSettings
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
                is ObjectArrayRecord -> RecordedObjectArray(record, renderSettings)
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
                node.styleClass.add("ulyp-call-tree")
            })
            return objectValue
        }
    }
}