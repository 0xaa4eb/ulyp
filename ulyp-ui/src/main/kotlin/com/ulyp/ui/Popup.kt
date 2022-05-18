package com.ulyp.ui

import com.ulyp.ui.looknfeel.Theme
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Modality
import javafx.stage.Stage

open class Popup() : Stage() {

    private val anchorPane = AnchorPane()

    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(Main.stage)

        anchorPane.prefHeight = 500.0
        anchorPane.prefWidth = 700.0

        val dialogScene = Scene(anchorPane)
        dialogScene.stylesheets.add(Theme.DARK.ulypCssPath)
        scene = dialogScene
    }

    var content: Node? = null
        get() = field
        set(value) {
            field = value

            anchorPane.children.add(value)
            AnchorPane.setTopAnchor(value, 50.0)
            AnchorPane.setBottomAnchor(value, 50.0)
            AnchorPane.setRightAnchor(value, 50.0)
            AnchorPane.setLeftAnchor(value, 50.0)
        }
}