package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.StringObjectRecord
import com.ulyp.ui.util.SingleLineTextView
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import com.ulyp.ui.util.TrimmedTextView
import javafx.event.EventHandler
import javafx.scene.text.Text

class RenderedString internal constructor(record: StringObjectRecord) : RenderedObject() {

    init {
        val fullText: Text = StyledText.of(
                SingleLineTextView(
                        Text("\"" + record.value() + "\"")
                ),
                Style.CALL_TREE,
                Style.CALL_TREE_STRING
        )
        val trimmedText: Text = StyledText.of(
                SingleLineTextView(
                        TrimmedTextView(
                                Text("\"" + record.value() + "\"")
                        )
                ),
                Style.CALL_TREE,
                Style.CALL_TREE_STRING
        )
        children += trimmedText

        if (trimmedText.text.length != fullText.text.length) {

            this.onMouseEntered = EventHandler {
                children.clear()
                children += fullText
            }
            this.onMouseExited = EventHandler {
                children.clear()
                children += trimmedText
            }
        }
    }
}