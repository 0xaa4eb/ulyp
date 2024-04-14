package com.ulyp.ui.elements.recording.tree

import com.ulyp.ui.settings.Settings
import com.ulyp.ui.util.Style
import javafx.event.EventHandler
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.springframework.beans.factory.annotation.Autowired

class RecordingTreeView(recording: RecordedCallTreeItem) : TreeView<RecordedCallNodeContent>(recording) {

    @Autowired
    private lateinit var settings: Settings

    init {
        styleClass += "ulyp-call-tree-view"
        styleClass += Style.ZERO_PADDING.cssClasses

        onKeyPressed = EventHandler { key: KeyEvent ->
            if (key.code == KeyCode.EQUALS) {
                settings.recordingTreeFontSize.value += 1
            }
            if (key.code == KeyCode.MINUS) {
                settings.recordingTreeFontSize.value -= 1
            }
        }

        /*
        source code view Currently disabled
        val sourceCodeFinder = SourceCodeFinder(processMetadata.classPathFiles)
               treeView!!.selectionModel.selectedItemProperty()
                       .addListener { observable: ObservableValue<out TreeItem<RecordedCallNodeContent>?>?, oldValue: TreeItem<RecordedCallNodeContent>?, newValue: TreeItem<RecordedCallNodeContent>? ->
                           val selectedNode = newValue as RecordedCallTreeItem?
                           if (selectedNode?.callRecord != null) {
                               val sourceCodeFuture = sourceCodeFinder.find(
                                       selectedNode.callRecord.method.declaringType.name
                               )
                               sourceCodeFuture.thenAccept { sourceCode: SourceCode? ->
                                   Platform.runLater {
                                       val currentlySelected = treeView!!.selectionModel.selectedItem
                                       val currentlySelectedNode = currentlySelected as RecordedCallTreeItem
                                       if (selectedNode.callRecord.id == currentlySelectedNode.callRecord.id) {
                                           sourceCodeView.setText(sourceCode, currentlySelectedNode.callRecord.method.name)
                                       }
                                   }
                               }
                           }
                       }*/
    }
}