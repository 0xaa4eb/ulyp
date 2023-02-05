package com.ulyp.ui.elements.recording.tree

import com.ulyp.ui.RenderSettings
import com.ulyp.ui.util.FxThreadExecutor.execute
import javafx.event.EventHandler
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class FileRecordingTabPane : TabPane() {

    @Autowired
    private lateinit var context: ApplicationContext

    @Autowired
    private lateinit var renderSettings: RenderSettings

    private var lastSelectedOnShowTypes: RecordedCallTreeItem? = null

    fun clear() {
        for (tab in tabs) {
            val fileRecordingsTab = tab as FileRecordingsTab
            fileRecordingsTab.dispose()
        }
        tabs.forEach { it.onClosed.handle(null) }
        tabs.clear()
    }

    private val selectedTab: FileRecordingsTab
        get() = selectionModel.selectedItem as FileRecordingsTab

    fun getOrCreateProcessTab(name: FileRecordingsTabName): FileRecordingsTab {
        return execute {
            val processTab = tabs
                    .stream()
                    .filter { tab: Tab -> name == (tab as FileRecordingsTab).name }
                    .findFirst()
            if (processTab.isPresent) {
                return@execute processTab.get() as FileRecordingsTab
            } else {
                val tab = context.getBean(FileRecordingsTab::class.java, name, context)
                tabs.add(tab)
                return@execute tab
            }
        }
    }

    fun keyPressed(event: KeyEvent) {
        if (event.code == KeyCode.SHIFT) {
            val selected = selectedTab.selectedTreeTab.getSelected()
            @Suppress("SENSELESS_COMPARISON")
            if (selected != null) {
                renderSettings.showTypes = true
                lastSelectedOnShowTypes = selected
                selected.refresh()
            }
        } else {
            if (event.isControlDown && event.code == KeyCode.C) {
                // COPY currently selected
                val selectedTab: FileRecordingsTab? = selectedTab
                if (selectedTab != null) {
                    val selectedCallRecord: RecordedCallTreeItem = selectedTab.selectedTreeTab.getSelected()
                    @Suppress("SENSELESS_COMPARISON")
                    if (selectedCallRecord != null) {
                        val clipboard = Clipboard.getSystemClipboard()
                        val content = ClipboardContent()
                        content.putString(selectedCallRecord.toClipboardText())
                        clipboard.setContent(content)
                    }
                }
            }
        }
    }

    fun keyReleased(event: KeyEvent) {
        if (event.code == KeyCode.SHIFT) {
            renderSettings.showTypes = false
            if (lastSelectedOnShowTypes != null) {
                lastSelectedOnShowTypes!!.refresh()
            } else {
                val selected = selectedTab.selectedTreeTab.getSelected()
                if (selected != null) {
                    selected.refresh()
                }
            }
        }
    }

    init {
        onKeyPressed = EventHandler { event: KeyEvent -> keyPressed(event) }
        onKeyReleased = EventHandler { event: KeyEvent -> keyReleased(event) }
    }
}