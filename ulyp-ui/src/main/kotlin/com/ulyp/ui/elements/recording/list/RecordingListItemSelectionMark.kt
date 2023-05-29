package com.ulyp.ui.elements.recording.list

import com.ulyp.ui.util.Style
import javafx.scene.text.Text

class RecordingListItemSelectionMark: Text("\uD83D\uDDF2 ") {
    init {
        this.styleClass.addAll(Style.BRIGHT_TEXT.cssClasses)
    }
}