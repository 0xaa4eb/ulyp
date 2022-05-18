package com.ulyp.ui.menu

import com.ulyp.ui.Main
import com.ulyp.ui.Popup
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.CssClass.ERROR_TEXT
import com.ulyp.ui.util.StyledText
import javafx.scene.Node
import javafx.scene.image.Image
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "prototype")
class ErrorPopup(errorTitle: String, errorContent: String) : Popup() {

    init {
        this.content = StyledText.of(errorContent, ERROR_TEXT)
        this.title = errorTitle

        val iconStream = Main::class.java.classLoader.getResourceAsStream("icons/error-icon.png")
                ?: throw RuntimeException("Icon not found")
        icons.add(Image(iconStream))
    }
}