package com.ulyp.ui

import com.ulyp.core.*
import com.ulyp.transport.RecordingInfo
import com.ulyp.ui.code.SourceCode
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.code.find.SourceCodeFinder
import com.ulyp.ui.looknfeel.FontSizeChanger
import com.ulyp.ui.util.ClassNameUtils.toSimpleName
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Tab
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Region
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.sql.Timestamp

@Component
@Scope(value = "prototype")
class CallRecordTreeTab(
    private val parent: Region,
    private val database: CallRecordDatabase?,
    private val methodInfoDatabase: MethodInfoDatabase,
    private val typeInfoDatabase: TypeInfoDatabase
) : Tab() {
    private var root: CallRecord? = null
    private var recordingInfo: RecordingInfo? = null
    private var treeView: TreeView<CallTreeNodeContent>? = null

    @Autowired
    private val sourceCodeView: SourceCodeView? = null
    @Autowired
    private val renderSettings: RenderSettings? = null
    @Autowired
    private val fontSizeChanger: FontSizeChanger? = null

    private var initialized = false

    @Synchronized
    fun init() {
        if (initialized) {
            return
        }
        treeView = TreeView(CallRecordTreeNode(database!!, root!!.id, renderSettings))
        treeView!!.prefHeightProperty().bind(parent.heightProperty())
        treeView!!.prefWidthProperty().bind(parent.widthProperty())
        val sourceCodeFinder = SourceCodeFinder(recordingInfo!!.processInfo.classpathList)
        treeView!!.selectionModel.selectedItemProperty()
            .addListener { observable: ObservableValue<out TreeItem<CallTreeNodeContent>?>?, oldValue: TreeItem<CallTreeNodeContent>?, newValue: TreeItem<CallTreeNodeContent>? ->
                val selectedNode = newValue as CallRecordTreeNode?
                if (selectedNode?.callRecord != null) {
                    val sourceCodeFuture = sourceCodeFinder.find(
                        selectedNode.callRecord!!.className
                    )
                    sourceCodeFuture.thenAccept { sourceCode: SourceCode? ->
                        Platform.runLater {
                            val currentlySelected = treeView!!.selectionModel.selectedItem
                            val currentlySelectedNode = currentlySelected as CallRecordTreeNode
                            if (selectedNode.callRecord!!.id == currentlySelectedNode.callRecord!!.id) {
                                sourceCodeView!!.setText(sourceCode, currentlySelectedNode.callRecord!!.methodName)
                            }
                        }
                    }
                }
            }
        treeView!!.onKeyPressed = EventHandler { key: KeyEvent ->
            if (key.code == KeyCode.EQUALS) {
                fontSizeChanger!!.upscale(parent.scene)
            }
            if (key.code == KeyCode.MINUS) {
                fontSizeChanger!!.downscale(parent.scene)
            }
        }
        text = tabName
        content = treeView
        onClosed = EventHandler { ev: Event? -> dispose() }
        tooltip = tooltipText
        initialized = true
    }

    @get:Synchronized
    val tabName: String
        get() = if (root == null || recordingInfo == null || database == null) {
            "?"
        } else recordingInfo!!.threadName + " " +
                toSimpleName(root!!.className) + "." + root!!.methodName + "(" + recordingInfo!!.lifetimeMillis + " ms, " + database.countAll() + ")"

    @get:Synchronized
    private val tooltipText: Tooltip
        private get() {
            if (root == null || recordingInfo == null || database == null) {
                return Tooltip("")
            }
            val builder = StringBuilder()
                .append("Thread: ").append(recordingInfo!!.threadName).append("\n")
                .append("Created at: ").append(Timestamp(recordingInfo!!.createEpochMillis)).append("\n")
                .append("Finished at: ")
                .append(Timestamp(recordingInfo!!.createEpochMillis + recordingInfo!!.lifetimeMillis)).append("\n")
                .append("Lifetime: ").append(recordingInfo!!.lifetimeMillis).append(" millis").append("\n")
            builder.append("Stack trace: ").append("\n")
            for (element in recordingInfo!!.stackTrace.elementList) {
                builder.append("\tat ")
                    .append(element.declaringClass)
                    .append(".")
                    .append(element.methodName)
                    .append("(")
                    .append(element.fileName)
                    .append(":")
                    .append(element.lineNumber)
                    .append(")")
                    .append("\n")
            }
            return Tooltip(builder.toString())
        }

    fun getSelected(): CallRecordTreeNode? {
        return treeView!!.selectionModel.selectedItem as CallRecordTreeNode
    }

    fun dispose() {
        database!!.close()
    }

    @Synchronized
    fun refreshTreeView() {
        init()
        val root = treeView!!.root as CallRecordTreeNode
        text = tabName
        root.refresh()
    }

    @Synchronized
    fun uploadChunk(chunk: CallRecordTreeChunk) {
        try {
            if (recordingInfo == null) {
                recordingInfo = chunk.recordingInfo
            }
            methodInfoDatabase.addAll(MethodInfoList(chunk.request.methodDescriptionList.data))
            typeInfoDatabase.addAll(chunk.request.descriptionList)
            database!!.persistBatch(
                CallEnterRecordList(chunk.request.recordLog.enterRecords),
                CallExitRecordList(chunk.request.recordLog.exitRecords)
            )
            if (root == null) {
                root = database.root
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}