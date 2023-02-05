package com.ulyp.ui

import com.ulyp.ui.reader.Filter
import com.ulyp.ui.reader.FilterRegistry
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextField
import java.net.URL
import java.util.*


class FilterView(private val filterRegistry: FilterRegistry) : Initializable {

    @FXML
    lateinit var minimumCallCount: TextField
    @FXML
    lateinit var applyButton: Button

    override fun initialize(url: URL, rb: ResourceBundle?) {

    }

    fun apply() {
        filterRegistry.filter = Filter(minimumCallCount.text.toInt())
    }
}