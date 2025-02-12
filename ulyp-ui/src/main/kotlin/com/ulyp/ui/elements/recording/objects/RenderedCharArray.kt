package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.arrays.CharArrayRecord
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of

class RenderedCharArray(record: CharArrayRecord) : RenderedObject() {

    init {
        children.addAll(
                listOf(
                        of("char", Style.CALL_TREE_TYPE_NAME),
                        of("[", Style.CALL_TREE_COLLECTION_BRACKET),
                        of(record.length.toString(), Style.CALL_TREE_TYPE_NAME),
                        of("]", Style.CALL_TREE_COLLECTION_BRACKET),
                        of("@", Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT),
                        of(Integer.toHexString(record.identityRecord.hashCode), Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT)
                )
        )
    }
}