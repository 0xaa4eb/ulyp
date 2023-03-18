package com.ulyp.ui.elements.recording.list

import com.ulyp.storage.Recording
import javafx.collections.ListChangeListener
import javafx.scene.control.ListView
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * A tab which contains all recordings for a particular recording file. It itself contains
 * a set of tabs of type {@link RecordingTab} in it (one such tab per every recording)
 */
@Component
@Scope(scopeName = "prototype")
class RecordingsListView internal constructor() : ListView<RecordingListItem>() {

    private val recordingIds = mutableMapOf<Int, RecordingListItem>()

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

    }

    fun createOrUpdate(recording: Recording) {
        val item = RecordingListItem(recording)
        val fromStateItem = recordingIds.computeIfAbsent(item.recordingId) {
            items.add(item)
            item
        }
        if (recording.isComplete) {
            fromStateItem.updateLifetime(recording)
        }
    }
}