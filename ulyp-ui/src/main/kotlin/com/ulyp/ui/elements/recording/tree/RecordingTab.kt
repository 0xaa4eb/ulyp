package com.ulyp.ui.elements.recording.tree

import com.ulyp.core.ProcessMetadata
import com.ulyp.storage.tree.CallRecord
import com.ulyp.storage.tree.Recording
import com.ulyp.ui.RenderSettings
import com.ulyp.ui.code.SourceCode
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.code.find.SourceCodeFinder
import com.ulyp.ui.looknfeel.FontStyleUpdater
import com.ulyp.ui.settings.Settings
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * A tab which contains a particular recording (i.e. particular recorded method call including
 * all its nested calls)
 */
@Component
@Scope(value = "prototype")
class RecordingTab(
        private val parent: Region,
        private val processMetadata: ProcessMetadata,
        private val recording: Recording
) : VBox() {

    val recordingId = recording.id
    private var root: CallRecord? = null
    private var treeView: RecordingTreeView? = null

    @Autowired
    private lateinit var sourceCodeView: SourceCodeView
    @Autowired
    private lateinit var renderSettings: RenderSettings
    @Autowired
    private lateinit var settings: Settings

    private var initialized = false

    @Synchronized
    fun init() {
        if (initialized) {
            return
        }

        treeView = RecordingTreeView(RecordedCallTreeItem(recording, root!!.id, renderSettings), settings, processMetadata, sourceCodeView)

        treeView!!.prefHeightProperty().bind(parent.heightProperty())
        treeView!!.prefWidthProperty().bind(parent.widthProperty())

        children.add(treeView)
        // TODO tooltip = tooltipText
        initialized = true
    }

    fun getSelected(): RecordedCallTreeItem {
        return treeView!!.selectionModel.selectedItem as RecordedCallTreeItem
    }

    fun dispose() {
    }

    @Synchronized
    fun refreshTreeView() {
        init()
        val root = treeView!!.root as RecordedCallTreeItem
        root.refresh()
    }

    @Synchronized
    fun update(recording: Recording) {
        if (root == null) {
            root = recording.root
        }
    }
}