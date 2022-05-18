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
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import java.io.File
import java.net.URL
import java.util.*
import java.util.function.Supplier


class PrimaryViewController(
        private val sourceCodeView: SourceCodeView,
        private val processTabPane: ProcessTabPane,
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
        val popup = Popup(StyledText.of("This is a text", CssClass.HELP_TEXT))
        popup.show()
    }

    fun showControlsPopup(event: Event?) {

    }

    fun changeTheme(event: Event?) {
        themeManager.applyTheme(Theme.LIGHT, primaryPane.scene)
    }

    fun openRecordedDump(actionEvent: ActionEvent?) {
        // Without those calls font style won't be applied until user changes font for the first time
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