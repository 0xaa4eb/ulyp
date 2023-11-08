package com.ulyp.ui.elements.recording.tree

import com.ulyp.ui.RenderSettings
import com.ulyp.ui.settings.Settings
import com.ulyp.ui.util.FxThreadExecutor
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
    @Autowired
    private lateinit var settings: Settings

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
        return FxThreadExecutor.execute {
            val processTab = tabs
                    .stream()
                    .filter { tab: Tab -> name == (tab as FileRecordingsTab).name }
                    .findFirst()
            if (processTab.isPresent) {
                return@execute processTab.get() as FileRecordingsTab
            } else {
                val tab = context.getBean(FileRecordingsTab::class.java, name, context, settings)
                tabs.add(tab)
                return@execute tab
            }
        }
    }

    fun keyPressed(event: KeyEvent) {
        if (event.code == KeyCode.SHIFT) {


            selectedTab.selectedTreeTab?.getSelected()?.let {
                renderSettings.showTypes = true
                lastSelectedOnShowTypes = it
                it.refresh()
            }
        } else {
            if (event.isControlDown && event.code == KeyCode.C) {
                // COPY currently selected

                selectedTab.selectedTreeTab?.getSelected()?.let {
                    val clipboard = Clipboard.getSystemClipboard()
                    val content = ClipboardContent()
                    content.putString(it.toClipboardText())
                    clipboard.setContent(content)
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
                selectedTab.selectedTreeTab?.getSelected()?.refresh()
            }
        }
    }

    init {
        onKeyPressed = EventHandler { event: KeyEvent -> keyPressed(event) }
        onKeyReleased = EventHandler { event: KeyEvent -> keyReleased(event) }
    }
}