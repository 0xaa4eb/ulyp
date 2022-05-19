package com.ulyp.ui.elements.controls

import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.collections.FXCollections
import javafx.scene.control.ListView
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "prototype")
class ControlsPopup() : Popup() {

    init {
        val items = FXCollections.observableArrayList(
                StyledText.of("+: Increase font size", Style.HELP_TEXT),
                StyledText.of("-: Decrease font size", Style.HELP_TEXT),
                StyledText.of("Hold shift: Show full type names", Style.HELP_TEXT)
        )

        val list = ListView(items)
        content = list
    }
}