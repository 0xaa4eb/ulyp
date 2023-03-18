package com.ulyp.ui.elements.recording.list

import com.ulyp.storage.Recording
import com.ulyp.ui.util.ClassNameUtils
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import java.sql.Timestamp
import java.time.format.DateTimeFormatter

class RecordingListItem(recording: Recording): TextFlow() {

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss.SSS")
    }

    val recordingId = recording.id

    init {
        val recordingMetadata = recording.metadata
        val rootCallRecord = recording.root
        val timestampText = Text(Timestamp(recordingMetadata.recordingStartedEpochMillis).toLocalDateTime().format(dateTimeFormatter))
        children.add(StyledText.of(timestampText, Style.RECORDING_LIST_ITEM, Style.SMALLER_TEXT))
        children.add(Text(" "))
        children.add(StyledText.of(recordingMetadata.threadName, Style.RECORDING_LIST_ITEM, Style.SMALLER_TEXT))
        children.add(Text(" "))
        children.add(StyledText.of(
            "${ClassNameUtils.toSimpleName(rootCallRecord.method.declaringType.name)}.${rootCallRecord.method.name}",
            Style.RECORDING_LIST_ITEM
        ))
        children.add(
            StyledText.of(
                " (" + recording.lifetime.toMillis() + " ms, " + recording.callCount() + ")",
                Style.RECORDING_LIST_ITEM
            )
        )
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
    }

    class SelectionMark(): Text("✔ ") {
        init {

        }
    }

    fun markSelected() {
        if (children.none { it is SelectionMark }) {
            this.children.add(0, SelectionMark())
        }
    }

    fun clearSelection() {
        this.children.removeIf { it is SelectionMark }
    }

    fun updateLifetime(recording: Recording) {
        children[5] = StyledText.of(
            " (" + recording.lifetime.toMillis() + " ms, " + recording.callCount() + ")",
            Style.RECORDING_LIST_ITEM
        )
    }
}