package com.ulyp.ui.settings.props

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener

class BooleanProperty(initialValue: Boolean) : SimpleBooleanProperty(initialValue) {

    fun addWeakListener(listener: ChangeListener<in Boolean>) {
        super.addListener(WeakListener(listener))
    }
}