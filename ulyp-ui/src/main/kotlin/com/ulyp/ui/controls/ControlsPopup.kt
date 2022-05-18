package com.ulyp.ui.controls

import com.ulyp.ui.Popup
import com.ulyp.ui.util.CssClass
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
                StyledText.of("+: Increase font size", CssClass.HELP_TEXT),
                StyledText.of("-: Decrease font size", CssClass.HELP_TEXT),
                StyledText.of("Hold shift: Show full type names", CssClass.HELP_TEXT)
        )

        val list = ListView(items)
        content = list
    }
}