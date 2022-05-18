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

    fun clear() {
        for (tab in tabs) {
            val fileRecordingsTab = tab as FileRecordingsTab
            fileRecordingsTab.dispose()
        }
        tabs.clear()
    }

    val selectedTab: FileRecordingsTab
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
            if (selected != null) {
                renderSettings.setShowTypes(true)
                selected.refresh()
            }
        } else {
            if (event.isControlDown && event.code == KeyCode.C) {
                // COPY currently selected
                val selectedTab: FileRecordingsTab? = selectedTab
                if (selectedTab != null) {
                    val selectedCallRecord = selectedTab.selectedTreeTab.getSelected()
                    if (selectedCallRecord != null) {
                        val clipboard = Clipboard.getSystemClipboard()
                        val content = ClipboardContent()
                        val callRecord = selectedCallRecord.callRecord
                        content.putString(callRecord!!.method.declaringType.name + "." + callRecord.method.name)
                        clipboard.setContent(content)
                    }
                }
            }
        }
    }

    fun keyReleased(event: KeyEvent) {
        if (event.code == KeyCode.SHIFT) {
            val selected = selectedTab.selectedTreeTab.getSelected()
            if (selected != null) {
                renderSettings.setShowTypes(false)
                selected.refresh()
            }
        }
    }

    init {
        onKeyPressed = EventHandler { event: KeyEvent -> keyPressed(event) }
        onKeyReleased = EventHandler { event: KeyEvent -> keyReleased(event) }
    }
}