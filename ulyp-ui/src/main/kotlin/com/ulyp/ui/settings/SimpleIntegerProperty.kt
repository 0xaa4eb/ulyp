package com.ulyp.ui.settings

import javafx.beans.property.IntegerPropertyBase
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class SimpleIntegerProperty(initialValue: Int) : IntegerPropertyBase(initialValue) {

    override fun getBean(): Any? {
        return null
    }

    override fun getName(): String? {
        return null
    }

    fun toStringProperty(): StringProperty {
        val stringProp = SimpleStringProperty(this.get().toString())
        this.addListener { _, oldValue, newValue ->
            stringProp.set(newValue.toString())
        }
        return stringProp
    }
}