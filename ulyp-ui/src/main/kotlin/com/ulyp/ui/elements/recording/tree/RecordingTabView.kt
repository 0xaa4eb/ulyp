package com.ulyp.ui.elements.recording.tree

import com.ulyp.core.ProcessMetadata
import com.ulyp.storage.tree.Recording
import com.ulyp.ui.util.FxThreadExecutor
import javafx.application.Platform
import javafx.scene.layout.VBox
import org.springframework.context.ApplicationContext
import java.util.concurrent.ConcurrentHashMap

class RecordingTabView(private val applicationContext: ApplicationContext) : VBox() {

    private val tabsByRecordingId: MutableMap<Int, RecordingTab> = ConcurrentHashMap()
    var currentTab: RecordingTab? = null

    private fun getOrCreateRecordingTab(processMetadata: ProcessMetadata, recording: Recording): RecordingTab {
        val id = recording.id
        return FxThreadExecutor.execute {
            tabsByRecordingId.computeIfAbsent(id) { recordingId: Int ->
                val tab = applicationContext.getBean(
                    RecordingTab::class.java,
                    this,
                    processMetadata,
                    recording
                )
                tab
            }
        }
    }

    fun selectTab(recordingId: Int) {
        if (currentTab != null && currentTab!!.recordingId == recordingId) {
            return
        }

        currentTab = tabsByRecordingId[recordingId]
        children.clear()
        children.add(currentTab)
    }

    fun updateOrCreateRecordingTab(processMetadata: ProcessMetadata, recording: Recording): RecordingTab {
        val recordingTab = getOrCreateRecordingTab(processMetadata, recording)
        recordingTab.update(recording)
        Platform.runLater { recordingTab.refreshTreeView() }
        return recordingTab
    }

    fun dispose() {
        for (tab in tabsByRecordingId.values) {
            tab.dispose()
        }
    }
}