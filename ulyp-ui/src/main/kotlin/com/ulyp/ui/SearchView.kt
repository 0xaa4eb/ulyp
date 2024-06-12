package com.ulyp.ui

import com.ulyp.storage.search.PlainTextSearchQuery
import com.ulyp.storage.search.SearchDataReaderJob
import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.elements.recording.tree.FileRecordingsTab
import com.ulyp.ui.reader.ReaderRegistry
import com.ulyp.ui.util.SearchListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import java.net.URL
import java.util.*

class SearchView : Initializable {

    @FXML
    lateinit var searchTextField: TextField
    @Autowired
    lateinit var fileRecordingTabPane: FileRecordingTabPane
    @Autowired
    lateinit var readerRegistry: ReaderRegistry

    var stage: Stage? = null

    override fun initialize(url: URL, rb: ResourceBundle?) {
    }

    fun apply() {
        val selectedFileTab = fileRecordingTabPane.selectionModel.selectedItem as FileRecordingsTab?
        if (selectedFileTab != null) {
            val dataReader = readerRegistry.getByFile(selectedFileTab.name.file)

            if (dataReader == null) {
                return
            }

            dataReader.submitReaderJob(
                SearchDataReaderJob(PlainTextSearchQuery(searchTextField.text), SearchListener(selectedFileTab))
            )
            stage?.close()
        }
    }
}