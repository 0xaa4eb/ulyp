package com.ulyp.ui.settings

import javafx.beans.property.IntegerPropertyBase

class SimpleIntegerProperty(initialValue: Int) : IntegerPropertyBase(initialValue) {

    override fun getBean(): Any? {
        return null
    }

    override fun getName(): String? {
        return null
    }
}