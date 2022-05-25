package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.*
import com.ulyp.ui.RenderSettings
import javafx.scene.Node
import javafx.scene.text.TextFlow
import java.util.function.Consumer

abstract class RecordedObject protected constructor() : TextFlow() {
    companion object {
        @JvmStatic
        fun of(record: ObjectRecord, renderSettings: RenderSettings): RecordedObject {
            val objectValue = when (record) {
                is StringObjectRecord -> RecordedString(record)
                is CharObjectRecord -> RecordedChar(record)
                is NullObjectRecord -> RecordedNull()
                is NotRecordedObjectRecord -> NotRecordedObject(renderSettings)
                is NumberRecord -> RecordedNumber(record.numberPrintedText, record.getType(), renderSettings)
                is FileRecord -> RecordedFile(record.path, record.getType(), renderSettings)
                is ObjectArrayRecord -> RecordedObjectArray(record, renderSettings)
                is CollectionRecord -> RecordedCollection(record, renderSettings)
                is MapEntryRecord -> RecordedMapEntry(record, renderSettings)
                is ClassObjectRecord -> RecordedClassObject(record, renderSettings)
                is MapRecord -> RecordedMap(record, renderSettings)
                is IdentityObjectRecord -> RecordedIdentityObject(record, renderSettings)
                is ThrowableRecord -> RecordedException(record, renderSettings)
                is EnumRecord -> RecordedEnum(record, renderSettings)
                is PrintedObjectRecord -> RecordedPrintedObject(record, renderSettings)
                is DateRecord -> RecordedDate(record, renderSettings)
                is BooleanRecord -> RecordedBoolean(record, renderSettings)
                is OptionalRecord -> RecordedOptional(record, renderSettings)
                else -> throw RuntimeException("Not supported for rendering: $record")
            }
            objectValue.children.forEach(Consumer { node: Node ->
                node.styleClass.add("ulyp-ctt")
            })
            return objectValue
        }
    }
}