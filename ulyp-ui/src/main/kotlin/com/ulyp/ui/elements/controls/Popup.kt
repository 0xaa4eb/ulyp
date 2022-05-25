package com.ulyp.ui.elements.controls

import com.ulyp.ui.Main
import com.ulyp.ui.SceneRegistry
import javafx.scene.Node
import javafx.scene.layout.AnchorPane
import javafx.stage.Modality
import javafx.stage.Stage

open class Popup(sceneRegistry: SceneRegistry) : Stage() {

    private val anchorPane = AnchorPane()

    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(Main.stage)

        anchorPane.prefHeight = 500.0
        anchorPane.prefWidth = 700.0

        val dialogScene = sceneRegistry.newScene(anchorPane)
        scene = dialogScene
    }

    var content: Node? = null
        set(value) {
            field = value

            anchorPane.children.add(value)
            AnchorPane.setTopAnchor(value, 50.0)
            AnchorPane.setBottomAnchor(value, 50.0)
            AnchorPane.setRightAnchor(value, 50.0)
            AnchorPane.setLeftAnchor(value, 50.0)
        }
}