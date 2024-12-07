package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.ThreadRecord
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText.of
import javafx.scene.Node

class RenderedThread(record: ThreadRecord, renderSettings: RenderSettings) : RenderedObject() {

    init {
        val nodes = mutableListOf<Node>()
        if (renderSettings.showTypes) {
            nodes += of("java.lang.Thread", Style.CALL_TREE_TYPE_NAME)
        } else {
            nodes += of("Thread", Style.CALL_TREE_TYPE_NAME)
        }
        nodes += of("@", Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT)
        nodes += of(Integer.toHexString(record.identity.hashCode), Style.CALL_TREE_IDENTITY, Style.SMALLER_TEXT)
        nodes += of("(", Style.CALL_TREE_COLLECTION_BRACKET)

        if (renderSettings.showTypes) {
            nodes += of("tid: ", Style.CALL_TREE)
        }
        nodes += of("" + record.tid, Style.CALL_TREE_NUMBER)
        if (renderSettings.showTypes) {
            nodes += of(", name: ", Style.CALL_TREE)
        } else {
            nodes += of(",  ", Style.CALL_TREE)
        }
        nodes += of(record.name, Style.CALL_TREE_STRING)
        nodes += of(")", Style.CALL_TREE_COLLECTION_BRACKET)

        children.addAll(nodes)
    }
}