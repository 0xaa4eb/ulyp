package com.ulyp.ui

import com.ulyp.storage.impl.AsyncFileStorageReader
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.looknfeel.FontSizeChanger
import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.util.CssClass
import com.ulyp.ui.util.StyledText
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.util.*
import java.util.function.Supplier


class PrimaryViewController(
        private val sourceCodeView: SourceCodeView,
        private val processTabPane: ProcessTabPane,
        private val fontSizeChanger: FontSizeChanger,
        private val themeManager: ThemeManager,
        private val fileChooser: Supplier<File>
) : Initializable {

    @FXML
    lateinit var primaryPane: VBox
    @FXML
    lateinit var processTabAnchorPane: AnchorPane
    @FXML
    lateinit var sourceCodeViewAnchorPane: AnchorPane

    override fun initialize(url: URL, rb: ResourceBundle?) {
        processTabAnchorPane.children.add(processTabPane)
        AnchorPane.setTopAnchor(processTabPane, 0.0)
        AnchorPane.setBottomAnchor(processTabPane, 0.0)
        AnchorPane.setRightAnchor(processTabPane, 0.0)
        AnchorPane.setLeftAnchor(processTabPane, 0.0)
        sourceCodeViewAnchorPane.children.add(sourceCodeView)
        AnchorPane.setTopAnchor(sourceCodeView, 0.0)
        AnchorPane.setBottomAnchor(sourceCodeView, 0.0)
        AnchorPane.setRightAnchor(sourceCodeView, 0.0)
        AnchorPane.setLeftAnchor(sourceCodeView, 0.0)
    }

    fun clearAll(event: Event?) {
        processTabPane.clear()
    }

    fun changeAggregation(event: Event?) {
        // TODO maybe implement
    }

    fun showAboutPopup(event: Event?) {
        val dialog = Stage()
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initOwner(Main.stage)
        val dialogVbox = AnchorPane()
        val text = StyledText.of("Ulyp 0.2, recording debugger", CssClass.TEXT)
        dialogVbox.children.add(text)

        AnchorPane.setTopAnchor(text, 50.0)
        AnchorPane.setBottomAnchor(text, 50.0)
        AnchorPane.setRightAnchor(text, 50.0)
        AnchorPane.setLeftAnchor(text, 50.0)

        val dialogScene = Scene(dialogVbox, 300.0, 200.0)
        dialogScene.stylesheets.add(Theme.DARK.ulypCssPath)
        dialog.scene = dialogScene
        dialog.show()
    }

    fun showControlsPopup(event: Event?) {

    }

    fun changeTheme(event: Event?) {
        themeManager.applyTheme(Theme.LIGHT, primaryPane.scene)
    }

    fun openRecordedDump(actionEvent: ActionEvent?) {
        // Without those calls font style won't be applied until user changes font for the first time
        fontSizeChanger.upscale(primaryPane.scene)
        fontSizeChanger.downscale(primaryPane.scene)
        val file = fileChooser.get()
        val storageReader = AsyncFileStorageReader(file, false)

        storageReader.processMetadataFuture.thenAccept { processMetadata ->
            storageReader.subscribe { recording ->
                val fileRecordingsTab = processTabPane.getOrCreateProcessTab(FileRecordingsTabName(file, processMetadata))
                val recordingTab = fileRecordingsTab.getOrCreateRecordingTab(processMetadata, recording)
                recordingTab.update(recording)
                Platform.runLater { recordingTab.refreshTreeView() }
            }
        }

        storageReader.start()
    }
}