package com.ulyp.ui

import com.ulyp.storage.Recording
import com.ulyp.storage.StorageReader
import com.ulyp.ui.util.FxThreadExecutor
import com.ulyp.ui.util.FxThreadExecutor.execute
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
@Scope(scopeName = "prototype")
class FileRecordingsTab internal constructor(
        val name: FileRecordingsTabName,
        private val applicationContext: ApplicationContext
        ) : Tab(name.toString()) {

    private lateinit var recordingTabs: TabPane

    private val tabsByRecordingId: MutableMap<Int, RecordingTab> = ConcurrentHashMap()

    @PostConstruct
    fun init() {
        val tabPane = TabPane()
        recordingTabs = tabPane
        content = tabPane
    }

    fun getOrCreateRecordingTab(storageReader: StorageReader, recording: Recording): RecordingTab {
        val id = recording.id
        return execute {
            tabsByRecordingId.computeIfAbsent(id) { recordingId: Int ->
                val tab = applicationContext.getBean(
                    RecordingTab::class.java,
                    recordingTabs,
                    storageReader,
                    recording
                )
                recordingTabs.tabs.add(tab)
                tab.onClosed = EventHandler { tabsByRecordingId.remove(recordingId) }
                tab
            }
        }
    }

    val selectedTreeTab: RecordingTab
        get() = recordingTabs.selectionModel.selectedItem as RecordingTab

    fun dispose() {
        for (tab in recordingTabs.tabs) {
            val fxRecordingTab = tab as RecordingTab
            fxRecordingTab.dispose()
        }
    }
}