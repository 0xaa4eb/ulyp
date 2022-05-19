package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.IdentityObjectRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import com.ulyp.ui.util.CssClass.*
import com.ulyp.ui.util.StyledText.of

class RecordedIdentityObject(record: IdentityObjectRecord, renderSettings: RenderSettings) : RecordedObject(record.type) {

    init {
        val className = if (renderSettings.showTypes()) record.type.name else toSimpleName(record.type.name)

        super.getChildren().addAll(
            listOf(
                of(className, CALL_TREE_TYPE_NAME_CSS),
                of("@", CALL_TREE_IDENTITY_CSS, CALL_TREE_IDENTITY_HASH_CODE_CSS),
                of(Integer.toHexString(record.hashCode), CALL_TREE_IDENTITY_CSS, CALL_TREE_IDENTITY_HASH_CODE_CSS)
            )
        )
    }
}