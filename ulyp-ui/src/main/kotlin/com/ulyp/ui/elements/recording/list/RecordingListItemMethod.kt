package com.ulyp.ui.elements.recording.list

import com.ulyp.storage.tree.Recording
import com.ulyp.ui.settings.Settings
import com.ulyp.ui.util.ClassNameUtils
import com.ulyp.ui.util.EnhancedText
import com.ulyp.ui.util.Style
import com.ulyp.ui.util.StyledText
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import java.sql.Timestamp
import java.time.format.DateTimeFormatter

class RecordingListItemMethod(private val recording: Recording, settings: Settings): TextFlow() {

    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss.SSS")
    }

    private var showThreadName: Boolean = false

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

    fun refreshName() {
        children.clear()

        val recordingMetadata = recording.metadata
        val rootCallRecord = recording.root
        children += EnhancedText(
            Timestamp(recordingMetadata.recordingStartedMillis).toLocalDateTime().format(dateTimeFormatter),
            Style.RECORDING_LIST_ITEM)
        children += Text(" ")
        if (showThreadName) {
            children += EnhancedText(recordingMetadata.threadName,
                Style.RECORDING_LIST_ITEM,
                Style.RECORDING_LIST_ITEM_THREAD,
                Style.HIDDEN)
        }
        children += Text(" ")
        children += EnhancedText(
            "${ClassNameUtils.toSimpleName(rootCallRecord.method.type.name)}.${rootCallRecord.method.name}",
            Style.RECORDING_LIST_ITEM,
            Style.BOLD_TEXT
        )
        children += EnhancedText(
            " (" + recording.lifetime.toMillis() + " ms, " + recording.callCount() + ")",
            Style.RECORDING_LIST_ITEM)
    }

    fun updateShowThreadName(showThreadName: Boolean) {
        this.showThreadName = showThreadName
        refreshName()
    }

    fun update(recording: Recording) {
        children[children.size - 1] = StyledText.of(
            " (" + recording.lifetime.toMillis() + " ms, " + recording.callCount() + ")",
            Style.RECORDING_LIST_ITEM
        )
    }

    fun markHighlighted() {
        children.forEach { it.styleClass.add("search-highlighted") }
    }

    fun clearHighlight() {
        children.forEach { it.styleClass.remove("search-highlighted") }
    }
}