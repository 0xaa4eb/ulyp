package com.ulyp.ui

import com.ulyp.transport.TCallRecordLogUploadRequest
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.looknfeel.FontSizeChanger
import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import org.springframework.beans.factory.annotation.Autowired
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.function.Supplier

class PrimaryViewController(
        private val sourceCodeView: SourceCodeView,
        private val processTabPane: ProcessTabPane,
        private val fontSizeChanger: FontSizeChanger,
        private val themeManager: ThemeManager,
        private val fileChooser: Supplier<File>
) : Initializable {

    @FXML
    var primaryPane: VBox? = null
    @FXML
    var processTabAnchorPane: AnchorPane? = null
    @FXML
    var sourceCodeViewAnchorPane: AnchorPane? = null

    private var aggregationStrategy: AggregationStrategy = ByRecordingIdAggregationStrategy()
    private val uploaderExecutorService = Executors.newFixedThreadPool(1)

    override fun initialize(url: URL, rb: ResourceBundle?) {
        processTabAnchorPane!!.children.add(processTabPane)
        AnchorPane.setTopAnchor(processTabPane, 0.0)
        AnchorPane.setBottomAnchor(processTabPane, 0.0)
        AnchorPane.setRightAnchor(processTabPane, 0.0)
        AnchorPane.setLeftAnchor(processTabPane, 0.0)
        sourceCodeViewAnchorPane!!.children.add(sourceCodeView)
        AnchorPane.setTopAnchor(sourceCodeView, 0.0)
        AnchorPane.setBottomAnchor(sourceCodeView, 0.0)
        AnchorPane.setRightAnchor(sourceCodeView, 0.0)
        AnchorPane.setLeftAnchor(sourceCodeView, 0.0)
    }

    fun clearAll(event: Event?) {
        processTabPane.clear()
    }

    fun changeAggregation(event: Event?) {
        aggregationStrategy = ByThreadIdAggregationStrategy()
        // TODO maybe popup
    }

    fun changeTheme(event: Event?) {
        themeManager.applyTheme(Theme.LIGHT, primaryPane!!.scene)
    }

    fun openRecordedDump(actionEvent: ActionEvent?) {
        // Without those calls font style won't be applied until user changes font for the first time
        fontSizeChanger.upscale(primaryPane!!.scene)
        fontSizeChanger.downscale(primaryPane!!.scene)
        val file = fileChooser.get()
        uploaderExecutorService.submit {
            try {
                BufferedInputStream(FileInputStream(file)).use { inputStream ->
                    while (inputStream.available() > 0) {
                        val request = TCallRecordLogUploadRequest.parseDelimitedFrom(inputStream)
                        val chunk = CallRecordTreeChunk(request)
                        val fileRecordingsTab = processTabPane.getOrCreateProcessTab(FileRecordingsTabName(file, chunk.processInfo))
                        val recordingTab = fileRecordingsTab.getOrCreateRecordingTab(aggregationStrategy, chunk)
                        recordingTab.uploadChunk(chunk)
                        Platform.runLater { recordingTab.refreshTreeView() }
                    }
                }
            } catch (e: Exception) {
                // TODO show error dialog
                e.printStackTrace()
            }
        }
    }
}