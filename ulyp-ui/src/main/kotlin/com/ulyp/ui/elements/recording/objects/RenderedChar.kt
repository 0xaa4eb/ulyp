package com.ulyp.ui.elements.recording.objects

import com.ulyp.core.recorders.basic.CharRecord
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import com.ulyp.ui.util.TrimmedTextView
import javafx.scene.text.Text

class RenderedChar internal constructor(value: CharRecord)
    : RenderedObject() {

    init {
        val text: Text = TrimmedTextView(Text("'" + value.value + "'"))
        children += StyledText.of(text.text, Style.CALL_TREE_STRING)
    }
}