package com.ulyp.ui

import com.ulyp.storage.util.PlainTextSearchQuery
import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.elements.recording.tree.FileRecordingsTab
import com.ulyp.ui.util.SearchListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.springframework.beans.factory.annotation.Autowired
import java.net.URL
import java.util.*

class SearchView() : Initializable {

    @FXML
    lateinit var searchTextField: TextField
    @FXML
    lateinit var searchApplyButton: Button
    @Autowired
    lateinit var fileRecordingTabPane: FileRecordingTabPane

    var stage: Stage? = null

    override fun initialize(url: URL, rb: ResourceBundle?) {
    }

    fun apply() {
        val selectedItem = fileRecordingTabPane.selectionModel.selectedItem as FileRecordingsTab?
/*        if (selectedItem != null) {
            selectedItem.recordingDataReader.initiateSearch(
                PlainTextSearchQuery(searchTextField.text),
                SearchListener(selectedItem)
            )
            stage?.close()
        }*/
    }
}