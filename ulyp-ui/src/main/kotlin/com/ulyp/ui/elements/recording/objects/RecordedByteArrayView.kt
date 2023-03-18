package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.arrays.ByteArrayRecord
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RecordedByteArrayView(record: ByteArrayRecord) : RecordedObjectView() {

    init {
        children.addAll(
                listOf(
                        of("byte", Style.CALL_TREE_TYPE_NAME),
                        of("[", Style.CALL_TREE_COLLECTION_BRACKET),
                        of(record.length.toString(), Style.CALL_TREE_TYPE_NAME),
                        of("]", Style.CALL_TREE_COLLECTION_BRACKET),
                        of("@", Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT),
                        of(Integer.toHexString(record.hashCode), Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT)
                )
        )
    }
}