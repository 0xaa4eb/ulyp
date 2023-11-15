package com.ulyp.ui.util

import com.ulyp.core.RecordedEnterMethodCall
import com.ulyp.core.RecordedExitMethodCall
import com.ulyp.storage.search.SearchResultListener
import com.ulyp.ui.elements.recording.tree.FileRecordingsTab
import javafx.application.Platform

class SearchListener(private val fileRecordingsTab: FileRecordingsTab) :
    SearchResultListener {

    private val map: MutableSet<Int> = mutableSetOf()

    override fun onStart() {

    }

    override fun onMatch(enterMethodCall: RecordedEnterMethodCall) {
/*        if (map.add(enterMethodCall.recordingId)) {
            Platform.runLater {
                fileRecordingsTab.recordingList.highlight(enterMethodCall.recordingId)
            }
        }*/
    }

    override fun onMatch(exitMethodCall: RecordedExitMethodCall) {

    }

    override fun onEnd() {

    }
}