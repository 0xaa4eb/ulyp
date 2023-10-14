package com.ulyp.ui.util

import com.ulyp.ui.settings.SimpleIntegerProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Slider

fun ChoiceBox<String>.connect(prop: StringProperty) {
    this.selectionModel.select(prop.value)
    this.setOnAction {
        prop.value = this.selectionModel.selectedItem
    }
}

fun Slider.connect(label: Label, prop: SimpleIntegerProperty) {
    label.text = prop.value.toString()
    this.value = prop.doubleValue()
    this.valueProperty().addListener { _, _, newValue ->
        label.text = newValue.toString()
        this.value = newValue.toInt().toDouble()
        prop.value = newValue.toInt()
    }
}