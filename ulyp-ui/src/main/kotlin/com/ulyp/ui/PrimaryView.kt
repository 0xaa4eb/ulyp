package com.ulyp.ui

import com.ulyp.core.exception.UlypException
import com.ulyp.core.repository.InMemoryRepository
import com.ulyp.core.repository.Repository
import com.ulyp.storage.impl.AsyncFileStorageReader
import com.ulyp.storage.impl.RecordedCallState
import com.ulyp.storage.impl.RocksdbIndex
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.elements.controls.ControlsPopup
import com.ulyp.ui.elements.controls.ErrorPopup
import com.ulyp.ui.elements.misc.ExceptionAsText
import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.elements.recording.tree.FileRecordingsTabName
import com.ulyp.ui.util.FxThreadExecutor
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Callback
import org.springframework.context.ApplicationContext
import java.io.File
import java.net.URL
import java.util.*
import java.util.function.Supplier
import kotlin.system.exitProcess


class PrimaryView(
        private val applicationContext: ApplicationContext,
        private val sourceCodeView: SourceCodeView,
        private val fileRecordingTabPane: FileRecordingTabPane,
        private val fileChooser: Supplier<File>
) : Initializable {

    @FXML
    lateinit var primaryPane: VBox

    @FXML
    lateinit var fileTabPaneAnchorPane: AnchorPane

    @FXML
    lateinit var sourceCodeViewAnchorPane: AnchorPane

    override fun initialize(url: URL, rb: ResourceBundle?) {
        fileTabPaneAnchorPane.children.add(fileRecordingTabPane)
        AnchorPane.setTopAnchor(fileRecordingTabPane, 0.0)
        AnchorPane.setBottomAnchor(fileRecordingTabPane, 0.0)
        AnchorPane.setRightAnchor(fileRecordingTabPane, 0.0)
        AnchorPane.setLeftAnchor(fileRecordingTabPane, 0.0)
        sourceCodeViewAnchorPane.children.add(sourceCodeView)
        AnchorPane.setTopAnchor(sourceCodeView, 0.0)
        AnchorPane.setBottomAnchor(sourceCodeView, 0.0)
        AnchorPane.setRightAnchor(sourceCodeView, 0.0)
        AnchorPane.setLeftAnchor(sourceCodeView, 0.0)
    }

    fun clearAll() {
        fileRecordingTabPane.clear()
    }

    fun showAboutPopup() {

    }

    fun exit() {
        exitProcess(0)
    }

    fun showSettings() {
        val loader = FXMLLoader(UIApplication::class.java.classLoader.getResource("SettingsView.fxml"))
        loader.controllerFactory = Callback { cl: Class<*>? -> applicationContext.getBean(cl) }

        val root = loader.load<Parent>()

        val scene = applicationContext.getBean(SceneRegistry::class.java).newScene(root)

        val stage = Stage()
        stage.scene = scene
        stage.isMaximized = false
        stage.title = "Ulyp Settings"
        val iconStream = UIApplication::class.java.classLoader.getResourceAsStream("icons/settings-icon.png")
                ?: throw UlypException("Icon not found")
        stage.icons.add(Image(iconStream))

        stage.show()
    }

    fun showControlsPopup() {
        val popup = applicationContext.getBean(ControlsPopup::class.java)
        popup.show()
    }

    fun openRecordedDump() {
        // Without those calls font style won't be applied until user changes font for the first time
        val file = fileChooser.get() ?: return
        val rocksdbAvailable = RocksdbIndex.checkIfRocksdbAvailable()
        val index: Repository<Long, RecordedCallState> = if (rocksdbAvailable.isSuccess) {
            RocksdbIndex()
        } else {
            InMemoryRepository()
        }
        val storageReader = AsyncFileStorageReader(file, false, index)

        storageReader.processMetadataFuture.thenAccept { processMetadata ->
            storageReader.subscribe { recording ->
                val fileRecordingsTab = fileRecordingTabPane.getOrCreateProcessTab(FileRecordingsTabName(file, processMetadata))
                val recordingTab = fileRecordingsTab.getOrCreateRecordingTab(processMetadata, recording)
                recordingTab.update(recording)
                Platform.runLater { recordingTab.refreshTreeView() }
            }
        }

        storageReader.start()

        storageReader.finishedReadingFuture.exceptionally {
            FxThreadExecutor.execute {
                val errorPopup = applicationContext.getBean(
                        ErrorPopup::class.java,
                        applicationContext.getBean(SceneRegistry::class.java),
                        "Stopped reading recording file $file with error: " + it.message,
                        ExceptionAsText(it)
                )
                errorPopup.show()
            }
            true
        }

        if (rocksdbAvailable.isFailure) {
            val errorPopup = applicationContext.getBean(
                ErrorPopup::class.java,
                applicationContext.getBean(SceneRegistry::class.java),
                "Rocksdb is not available on your platform, in-memory index will be used. Please note this may cause OOM on large recordings",
                ExceptionAsText(rocksdbAvailable.cause!!)
            )
            errorPopup.show()
        }
    }
}