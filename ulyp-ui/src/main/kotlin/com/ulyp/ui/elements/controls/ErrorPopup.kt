package com.ulyp.ui.elements.controls

import com.ulyp.ui.Main
import com.ulyp.ui.SceneRegistry
import javafx.scene.Node
import javafx.scene.image.Image
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "prototype")
class ErrorPopup(@Autowired sceneRegistry: SceneRegistry, errorTitle: String, content: Node)
    : Popup(sceneRegistry) {

    init {
        this.content = content
        this.title = errorTitle

        val iconStream = Main::class.java.classLoader.getResourceAsStream("icons/error-icon.png")
                ?: throw RuntimeException("Icon not found")
        icons.add(Image(iconStream))
    }
}