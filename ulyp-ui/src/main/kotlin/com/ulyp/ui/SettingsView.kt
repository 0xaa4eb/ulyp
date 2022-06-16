package com.ulyp.ui

import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.util.Settings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.text.Font
import java.net.URL
import java.util.*


class SettingsView(
        private val themeManager: ThemeManager,
        private val settings: Settings
) : Initializable {

    @FXML
    lateinit var themeChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var fontChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var recordingTreeFontSizeSlider: Slider
    @FXML
    lateinit var recordingTreeFontSizeLabel: Label

    override fun initialize(url: URL, rb: ResourceBundle?) {
        themeChoiceBox.items.addAll(Theme.values().map { it.name }.toList())
        themeChoiceBox.selectionModel.select(themeManager.currentTheme.name)
        themeChoiceBox.setOnAction {
            val selectedTheme: String = themeChoiceBox.selectionModel.selectedItem
            themeManager.applyTheme(Theme.valueOf(selectedTheme))
        }

        fontChoiceBox.items.addAll(Font.getFontNames())
        fontChoiceBox.selectionModel.select(settings.fontName)
        fontChoiceBox.setOnAction {
            val selectedFont: String = fontChoiceBox.selectionModel.selectedItem
            settings.fontName = selectedFont
        }

        recordingTreeFontSizeSlider.blockIncrement = 0.05

        recordingTreeFontSizeSlider.value = settings.recordingTreeFontSize
        recordingTreeFontSizeLabel.text = settings.recordingTreeFontSize.toString()

        recordingTreeFontSizeSlider.valueProperty().addListener {
            _, _, newValue ->
                settings.recordingTreeFontSize = newValue.toDouble()
                recordingTreeFontSizeLabel.text = settings.recordingTreeFontSize.toString()
        }
    }
}