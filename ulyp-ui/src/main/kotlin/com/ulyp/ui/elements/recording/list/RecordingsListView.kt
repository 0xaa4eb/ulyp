package com.ulyp.ui.elements.recording.list

import com.ulyp.storage.tree.Recording
import com.ulyp.ui.settings.Settings
import javafx.collections.ListChangeListener
import javafx.scene.control.ListView

/**
 * A tab which contains all recordings for a particular recording file. It itself contains
 * a set of tabs of type {@link RecordingTab} in it (one such tab per every recording)
 */
class RecordingsListView(val settings: Settings) : ListView<RecordingListItem>() {

    val recordings = mutableMapOf<Int, RecordingListItem>()

    init {
        selectionModel.selectedItems.addListener(
            ListChangeListener { change ->
                while (change.next()) {
                    if (change.wasAdded()) {
                        val recordingListItem = change.addedSubList[0];
                        recordingListItem?.let {
                            recordingListItem.markSelected()
                        }
                    }
                    if (change.wasRemoved()) {
                        change.removed.forEach {
                                unselected -> unselected?.clearSelection()
                        }
                    }
                }
            }
        )

        settings.recordingListShowThreads.addListener(ShowThreadNameListener(this))
    }

    fun createOrUpdate(recording: Recording) {
        val item = RecordingListItem(recording, settings)
        val fromStateItem = recordings.computeIfAbsent(item.recordingId) {
            items.add(item)
            item
        }
        fromStateItem.update(recording)
    }

    fun highlight(recordingId: Int) {
        recordings[recordingId]?.markHighlighted()
    }

    fun clearHighlights() {
        recordings.forEach { recordingId, recordingListItem -> recordingListItem.clearHighlight() }
    }
}