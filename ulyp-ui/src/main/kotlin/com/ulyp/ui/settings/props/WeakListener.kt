package com.ulyp.ui.settings.props

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.lang.ref.WeakReference

class WeakListener<T>(delegate: ChangeListener<T>) : ChangeListener<T> {

    private val delegateRef = WeakReference(delegate)

    override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
        delegateRef.get()?.changed(observable, oldValue, newValue)
    }
}