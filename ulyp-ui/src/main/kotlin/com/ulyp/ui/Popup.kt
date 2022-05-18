package com.ulyp.ui

import com.ulyp.ui.looknfeel.Theme
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Modality
import javafx.stage.Stage
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(value = "prototype")
class Popup(content: Node) : Stage() {

    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(Main.stage)
        val anchorPane = AnchorPane(content)
        anchorPane.prefHeight = 300.0
        anchorPane.prefWidth = 300.0

        AnchorPane.setTopAnchor(content, 50.0)
        AnchorPane.setBottomAnchor(content, 50.0)
        AnchorPane.setRightAnchor(content, 50.0)
        AnchorPane.setLeftAnchor(content, 50.0)

        val dialogScene = Scene(anchorPane)
        dialogScene.stylesheets.add(Theme.DARK.ulypCssPath)
        scene = dialogScene
    }
}