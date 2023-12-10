package com.ulyp.ui.elements.recording.list

import com.ulyp.storage.tree.Recording
import com.ulyp.ui.settings.Settings
import javafx.geometry.Pos
import javafx.scene.layout.StackPane

class RecordingListItem(recording: Recording, settings: Settings): StackPane() {

    val recordingId = recording.id
    private val selectionMark = RecordingListItemSelectionMark()
    private val method = RecordingListItemMethod(recording, settings)

    init {
        alignment = Pos.CENTER_LEFT
        styleClass += "ulyp-recording-list-item"

        children.addAll(selectionMark, method)
        selectionMark.prefHeightProperty().bind(this.prefHeightProperty())
        selectionMark.prefWidthProperty().bind(this.prefWidthProperty())
    }

/*        @get:Synchronized
        private val tooltipText: Tooltip
        private get() {
            if (root == null || recordingMetadata == null) {
                return Tooltip("")
            }
            val builder = StringBuilder()
                .append("Thread: ").append(recordingMetadata!!.threadName).append("\n")
                .append("Created at: ").append(Timestamp(recordingMetadata!!.recordingStartedEpochMillis)).append("\n")
                .append("Finished at: ")
                .append(Timestamp(recordingMetadata!!.recordingCompletedEpochMillis)).append("\n")
                .append("Lifetime: ").append(recording.lifetime.toMillis()).append(" millis").append("\n")

            builder.append("Stack trace: ").append("\n")
            recordingMetadata!!.stackTraceElements.forEach {
                builder.append("\t").append(it).append("\n")
            }

            val tooltip = Tooltip(builder.toString())
            tooltip.styleClass.addAll(Style.TOOLTIP_TEXT.cssClasses)
            return tooltip
        }*/

    private fun refreshName() {
        method.refreshName()
    }

    fun updateShowThreadName(showThreadName: Boolean) {
        method.updateShowThreadName(showThreadName)
        refreshName()
    }

    fun markSelected() {
        styleClass.add("selected")
    }

    fun clearSelection() {
        styleClass.remove("selected")
    }

    fun markHighlighted() {
//        this.children.add(RecordingListItemSelectionMark())
    }

    fun clearHighlight() {

    }

    fun update(recording: Recording) {
        method.update(recording)
    }
}