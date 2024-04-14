package com.ulyp.ui.elements.recording.tree

import com.ulyp.core.ProcessMetadata
import com.ulyp.ui.code.SourceCode
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.code.find.SourceCodeFinder
import com.ulyp.ui.settings.Settings
import com.ulyp.ui.util.Style
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

class RecordingTreeView(
    recording: RecordedCallTreeItem,
    private val settings: Settings,
    processMetadata: ProcessMetadata,
    private val sourceCodeView: SourceCodeView) : TreeView<RecordedCallNodeContent>(recording) {

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

        val sourceCodeFinder = SourceCodeFinder(processMetadata.classpath)
               selectionModel.selectedItemProperty()
                       .addListener { observable: ObservableValue<out TreeItem<RecordedCallNodeContent>?>?, oldValue: TreeItem<RecordedCallNodeContent>?, newValue: TreeItem<RecordedCallNodeContent>? ->
                           val selectedNode = newValue as RecordedCallTreeItem?
                           if (selectedNode?.callRecord != null) {
                               val sourceCodeFuture = sourceCodeFinder.find(
                                       selectedNode.callRecord.method.declaringType.name
                               )
                               sourceCodeFuture.thenAccept { sourceCode: SourceCode? ->
                                   Platform.runLater {
                                       val currentlySelected = selectionModel.selectedItem
                                       val currentlySelectedNode = currentlySelected as RecordedCallTreeItem
                                       if (selectedNode.callRecord.id == currentlySelectedNode.callRecord.id) {
                                           sourceCodeView.setText(sourceCode, currentlySelectedNode.callRecord.method.name)
                                       }
                                   }
                               }
                           }
                       }
    }
}