package com.ulyp.ui

import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.settings.Settings
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.text.Font
import java.net.URL
import java.util.*


class SettingsView(
    private val settings: Settings,
    private val themeManager: ThemeManager
) : Initializable {

    @FXML
    lateinit var themeChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var systemFontChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var systemFontSizeSlider: Slider
    @FXML
    lateinit var systemFontSizeLabel: Label
    @FXML
    lateinit var recordingTreeFontSizeSlider: Slider
    @FXML
    lateinit var recordingTreeFontSizeLabel: Label
    @FXML
    lateinit var recordingTreeFontChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var recordingTreeFontSpacingSlider: Slider
    @FXML
    lateinit var recordingTreeFontSpacingLabel: Label
    @FXML
    lateinit var recordingListShowThreads: CheckBox

    override fun initialize(url: URL, rb: ResourceBundle?) {

        themeChoiceBox.items.addAll(Theme.values().map { it.name }.toList())
        themeChoiceBox.selectionModel.select(settings.theme.get())
        themeChoiceBox.setOnAction {
            settings.theme.value = themeChoiceBox.selectionModel.selectedItem
        }

        systemFontChoiceBox.items.addAll(Font.getFamilies())
        systemFontChoiceBox.selectionModel.select(settings.systemFontName.value)
        systemFontChoiceBox.setOnAction {
            settings.systemFontName.value = systemFontChoiceBox.selectionModel.selectedItem
        }

        recordingTreeFontChoiceBox.items.addAll(Font.getFamilies())
        recordingTreeFontChoiceBox.selectionModel.select(settings.recordingTreeFontName.value)
        recordingTreeFontChoiceBox.setOnAction {
            settings.recordingTreeFontName.value = recordingTreeFontChoiceBox.selectionModel.selectedItem
        }

        systemFontSizeLabel.text = settings.systemFontSize.value.toString()
        systemFontSizeSlider.value = settings.systemFontSize.value.toDouble()
        systemFontSizeSlider.valueProperty().addListener { _, _, newValue ->
            systemFontSizeLabel.text = newValue.toString()
            systemFontSizeSlider.value = newValue.toInt().toDouble()
            settings.systemFontSize.value = newValue.toInt()
        }

        recordingTreeFontSizeLabel.text = settings.recordingTreeFontSize.value.toString()
        recordingTreeFontSizeSlider.value = settings.recordingTreeFontSize.value.toDouble()
        recordingTreeFontSizeSlider.valueProperty().addListener { _, _, newValue ->
            recordingTreeFontSizeLabel.text = newValue.toString()
            recordingTreeFontSizeSlider.value = newValue.toInt().toDouble()
            settings.recordingTreeFontSize.value = newValue.toInt()
        }

        recordingTreeFontSpacingLabel.text = settings.recordingTreeFontSpacing.value.toString()
        recordingTreeFontSpacingSlider.value = settings.recordingTreeFontSpacing.doubleValue()
        recordingTreeFontSpacingSlider.valueProperty().addListener { _, _, newValue ->
            recordingTreeFontSpacingLabel.text = newValue.toString()
            recordingTreeFontSpacingSlider.value = newValue.toInt().toDouble()
            settings.recordingTreeFontSpacing.value = newValue.toInt()
        }

        recordingListShowThreads.selectedProperty().bindBidirectional(settings.recordingListShowThreads)
    }
}