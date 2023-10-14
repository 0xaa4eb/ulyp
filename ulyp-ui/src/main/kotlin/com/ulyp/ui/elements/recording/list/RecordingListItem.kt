package com.ulyp.ui.elements.recording.list

import com.ulyp.storage.Recording
import com.ulyp.ui.settings.Settings
import com.ulyp.ui.util.ClassNameUtils
import com.ulyp.ui.util.EnhancedText
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import java.sql.Timestamp
import java.time.format.DateTimeFormatter

class RecordingListItem(private val recording: Recording, settings: Settings): TextFlow() {

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss.SSS")
    }

    private var showThreadName: Boolean = false
    val recordingId = recording.id

    init {
        this.showThreadName = settings.recordingListShowThreads.get()
        refreshName()
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
        children.clear()

        val recordingMetadata = recording.metadata
        val rootCallRecord = recording.root
        children.add(EnhancedText(
            Timestamp(recordingMetadata.recordingStartedEpochMillis).toLocalDateTime().format(dateTimeFormatter),
            Style.RECORDING_LIST_ITEM)
        )
        children.add(Text(" "))
        if (showThreadName) {
            children.add(EnhancedText(recordingMetadata.threadName,
                Style.RECORDING_LIST_ITEM,
                Style.RECORDING_LIST_ITEM_THREAD,
                Style.HIDDEN))
        }
        children.add(Text(" "))
        children.add(EnhancedText(
            "${ClassNameUtils.toSimpleName(rootCallRecord.method.declaringType.name)}.${rootCallRecord.method.name}",
            Style.RECORDING_LIST_ITEM,
            Style.BOLD_TEXT
        ))
        children.add(EnhancedText(
            " (" + recording.lifetime.toMillis() + " ms, " + recording.callCount() + ")",
            Style.RECORDING_LIST_ITEM)
        )
    }

    fun updateShowThreadName(showThreadName: Boolean) {
        this.showThreadName = showThreadName
        refreshName()
    }

    fun markSelected() {
        if (children.none { it is RecordingListItemSelectionMark }) {
            this.children.add(0, RecordingListItemSelectionMark())
        }
    }

    fun markHighlighted() {
        this.children.add(RecordingListItemSelectionMark())
    }

    fun clearHighlight() {
        // TODO
        this.children.removeIf { it is RecordingListItemSelectionMark }
    }

    fun clearSelection() {
        this.children.removeIf { it is RecordingListItemSelectionMark }
    }

    fun update(recording: Recording) {
        children[children.size - 1] = StyledText.of(
            " (" + recording.lifetime.toMillis() + " ms, " + recording.callCount() + ")",
            Style.RECORDING_LIST_ITEM
        )
    }
}