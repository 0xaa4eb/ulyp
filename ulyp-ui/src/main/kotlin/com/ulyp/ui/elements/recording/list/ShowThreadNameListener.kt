package com.ulyp.ui.elements.recording.list

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.lang.ref.WeakReference

class Listener(recordingsListView: RecordingsListView) : ChangeListener<Boolean> {

    private val recordingsListViewRef = WeakReference(recordingsListView)

    override fun changed(observable: ObservableValue<out Boolean>?, oldValue: Boolean?, newValue: Boolean?) {
        if (newValue != null) {
            recordingsListViewRef.get()?.recordings?.values?.forEach {
                it.updateShowThreadName(newValue)
            }
        }
    }
}