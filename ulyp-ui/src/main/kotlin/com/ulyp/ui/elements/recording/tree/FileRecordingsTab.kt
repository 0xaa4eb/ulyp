package com.ulyp.ui.elements.recording.tree

import com.ulyp.core.ProcessMetadata
import com.ulyp.storage.tree.Recording
import com.ulyp.ui.elements.recording.list.RecordingsListView
import com.ulyp.ui.settings.Settings
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.scene.control.SplitPane
import javafx.scene.control.Tab
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * A tab which contains all recordings for a particular recording file. It itself contains
 * a set of tabs of type {@link RecordingTab} in it (one such tab per every recording)
 */
@Component
@Scope(scopeName = "prototype")
class FileRecordingsTab internal constructor(
        val name: FileRecordingsTabName,
        private val applicationContext: ApplicationContext,
        settings: Settings
) : Tab(name.toString()) {

    val recordingList = RecordingsListView(settings)
    private lateinit var recordingTabView: RecordingTabView

    @PostConstruct
    fun init() {
        recordingTabView = RecordingTabView(applicationContext)
        val pane = SplitPane()
        pane.setDividerPosition(0, 0.2)
        pane.items.add(recordingList)
        pane.items.add(recordingTabView)
        content = pane

        recordingList.selectionModel.selectedItems.addListener(
            ListChangeListener {
                if (it.next()) {
                    val recordingListItem = it.addedSubList[0];
                    recordingListItem?.let {
                        recordingTabView.selectTab(it.recordingId)
                    }
                }
            }
        )
    }

    val selectedTreeTab: RecordingTab?
        get() = recordingTabView.currentTab

    fun dispose() {
        recordingTabView.dispose()
    }

    fun updateOrCreateRecordingTab(processMetadata: ProcessMetadata, recording: Recording) {
        recordingTabView.updateOrCreateRecordingTab(processMetadata, recording)
        Platform.runLater {
            recordingList.createOrUpdate(recording)
        }
    }
}