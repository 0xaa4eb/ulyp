package com.ulyp.ui

import com.ulyp.core.exception.UlypException
import com.ulyp.storage.tree.CallRecordTree
import com.ulyp.storage.util.RocksdbChecker
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.elements.controls.ControlsModalView
import com.ulyp.ui.elements.controls.ErrorModalView
import com.ulyp.ui.elements.misc.ExceptionAsTextView
import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.elements.recording.tree.FileRecordingsTabName
import com.ulyp.ui.reader.RecordingReaderRegistry
import com.ulyp.ui.settings.Settings
import com.ulyp.ui.util.FxThreadExecutor
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.SplitPane
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
    private val viewInitializer: ViewInitializer,
    private val sourceCodeView: SourceCodeView,
    private val readerRegistry: RecordingReaderRegistry,
    private val fileRecordingTabPane: FileRecordingTabPane,
    private val settings: Settings,
    private val fileChooser: Supplier<File?>
) : Initializable {

    @FXML
    lateinit var primaryPane: VBox
    @FXML
    lateinit var fileTabPaneAnchorPane: AnchorPane
    @FXML
    lateinit var primarySplitPane: SplitPane

    override fun initialize(url: URL, rb: ResourceBundle?) {
        fileTabPaneAnchorPane.children.add(fileRecordingTabPane)
        AnchorPane.setTopAnchor(fileRecordingTabPane, 0.0)
        AnchorPane.setBottomAnchor(fileRecordingTabPane, 0.0)
        AnchorPane.setRightAnchor(fileRecordingTabPane, 0.0)
        AnchorPane.setLeftAnchor(fileRecordingTabPane, 0.0)
        if (settings.sourceCodeViewerEnabled.get()) {
            val sourceCodeViewerAnchorPane = AnchorPane()
            primarySplitPane.items.add(sourceCodeViewerAnchorPane)
            sourceCodeViewerAnchorPane.children.add(sourceCodeView)
            AnchorPane.setTopAnchor(sourceCodeView, 0.0)
            AnchorPane.setBottomAnchor(sourceCodeView, 0.0)
            AnchorPane.setRightAnchor(sourceCodeView, 0.0)
            AnchorPane.setLeftAnchor(sourceCodeView, 0.0)
        }

        viewInitializer.init()
    }

    fun clearAll() {
        fileRecordingTabPane.clear()
    }

    fun showAboutPopup() {

    }

    fun exit() {
        exitProcess(0)
    }

    fun showFilterView() {
        val loader = FXMLLoader(UIApplication::class.java.classLoader.getResource("FilterView.fxml"))
        loader.controllerFactory = Callback { cl: Class<*>? -> applicationContext.getBean(cl) }
        val root = loader.load<Parent>()
        val scene = applicationContext.getBean(SceneRegistry::class.java).newScene(root)
        val stage = Stage()
        stage.scene = scene
        stage.isMaximized = false
        stage.title = "Filter recordings"
        val iconStream = UIApplication::class.java.classLoader.getResourceAsStream("icons/settings-icon.png") ?: throw UlypException("Icon not found")
        stage.icons.add(Image(iconStream))
        stage.show()
        val view = applicationContext.getBean(FilterView::class.java)
        view.stage = stage
    }

    fun showSearchView() {
        val loader = FXMLLoader(UIApplication::class.java.classLoader.getResource("SearchView.fxml"))
        loader.controllerFactory = Callback { cl: Class<*>? -> applicationContext.getBean(cl) }
        val root = loader.load<Parent>()
        val scene = applicationContext.getBean(SceneRegistry::class.java).newScene(root)
        val stage = Stage()
        stage.scene = scene
        stage.isMaximized = false
        stage.title = "Search"
        val iconStream = UIApplication::class.java.classLoader.getResourceAsStream("icons/settings-icon.png") ?: throw UlypException("Icon not found")
        stage.icons.add(Image(iconStream))
        stage.show()
        val view = applicationContext.getBean(SearchView::class.java)
        view.stage = stage
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
        val iconStream = UIApplication::class.java.classLoader.getResourceAsStream("icons/settings-icon.png") ?: throw UlypException("Icon not found")
        stage.icons.add(Image(iconStream))
        stage.show()
    }

    fun showControlsPopup() {
        val popup = applicationContext.getBean(ControlsModalView::class.java)
        popup.show()
    }

    fun openRecordingFile() {
        val file: File = fileChooser.get() ?: return
        val callRecordTree: CallRecordTree = readerRegistry.newCallRecordTree(file) ?: return

        val fileRecordingsTab = fileRecordingTabPane.getOrCreateProcessTab(
            FileRecordingsTabName(file, callRecordTree.processMetadata)
        )
        fileRecordingsTab.setOnClosed {
            readerRegistry.dispose(callRecordTree)
            callRecordTree.close()
        }

        val rocksdbAvailable = RocksdbChecker.checkRocksdbAvailable()
        if (!rocksdbAvailable.value()) {
            val errorPopup = applicationContext.getBean(
                ErrorModalView::class.java,
                applicationContext.getBean(SceneRegistry::class.java),
                "Rocksdb is not available on your platform, in-memory index will be used. Please note this may cause OOM on large recordings",
                ExceptionAsTextView(rocksdbAvailable.err!!)
            )
            errorPopup.show()
        }

        callRecordTree.subscribe {
            recording -> fileRecordingsTab.updateOrCreateRecordingTab(callRecordTree.processMetadata, recording)
        }

        callRecordTree.completeFuture.exceptionally {
            FxThreadExecutor.execute {
                val errorPopup = applicationContext.getBean(
                        ErrorModalView::class.java,
                        applicationContext.getBean(SceneRegistry::class.java),
                        "Stopped reading recording file $file with error: " + it.message,
                        ExceptionAsTextView(it)
                )
                errorPopup.show()
            }
            null
        }
    }
}