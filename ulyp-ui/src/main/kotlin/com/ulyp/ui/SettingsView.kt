package com.ulyp.ui

import com.ulyp.ui.looknfeel.FontSizeUpdater
import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.settings.SettingsStorage
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
    private val fontSizeUpdater: FontSizeUpdater,
    private val settingStorage: SettingsStorage
) : Initializable {

    @FXML
    lateinit var themeChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var fontChoiceBox: ChoiceBox<String>
    @FXML
    lateinit var systemFontSizeSlider: Slider
    @FXML
    lateinit var systemFontSizeLabel: Label
    @FXML
    lateinit var recordingTreeFontSizeSlider: Slider
    @FXML
    lateinit var recordingTreeFontSizeLabel: Label
    @FXML
    lateinit var recordingTreeFontSpacingSlider: Slider
    @FXML
    lateinit var recordingTreeFontSpacingLabel: Label

    override fun initialize(url: URL, rb: ResourceBundle?) {
        val currentSettings = settingStorage.read()
        val currentFontSettings = currentSettings.appearanceSettings.fontSettings

        themeChoiceBox.items.addAll(Theme.values().map { it.name }.toList())
        themeChoiceBox.selectionModel.select(themeManager.currentTheme.name)
        themeChoiceBox.setOnAction {
            val selectedTheme: String = themeChoiceBox.selectionModel.selectedItem
            themeManager.changeTheme(Theme.valueOf(selectedTheme))
        }

        // TODO remove duplication
        fontChoiceBox.items.addAll(Font.getFamilies())
        fontChoiceBox.selectionModel.select(currentFontSettings.recordingTreeFontName)
        fontChoiceBox.setOnAction {
            settingStorage.updateSettings { settings ->
                val selectedFont: String = fontChoiceBox.selectionModel.selectedItem
                settings.appearanceSettings.fontSettings.recordingTreeFontName = selectedFont
                fontSizeUpdater.update(UIApplication.stage.scene, settings.appearanceSettings.fontSettings)
            }
        }

        systemFontSizeLabel.text = currentFontSettings.systemFontSize.toString()
        systemFontSizeSlider.value = currentFontSettings.systemFontSize.toDouble()
        systemFontSizeSlider.valueProperty().addListener {_, _, newValue ->
            settingStorage.updateSettings { settings ->
                settings.appearanceSettings.fontSettings.systemFontSize = newValue.toInt()
                val roundedValue = settings.appearanceSettings.fontSettings.systemFontSize
                fontSizeUpdater.update(UIApplication.stage.scene, settings.appearanceSettings.fontSettings)
                systemFontSizeLabel.text = roundedValue.toString()
            }
        }

        recordingTreeFontSizeLabel.text = currentFontSettings.recordingTreeFontSize.toString()
        recordingTreeFontSizeSlider.value = currentFontSettings.recordingTreeFontSize.toDouble()
        recordingTreeFontSizeSlider.valueProperty().addListener {_, _, newValue ->
            settingStorage.updateSettings { settings ->
                settings.appearanceSettings.fontSettings.recordingTreeFontSize = newValue.toInt()
                val roundedValue = settings.appearanceSettings.fontSettings.recordingTreeFontSize
                fontSizeUpdater.update(UIApplication.stage.scene, settings.appearanceSettings.fontSettings)
                recordingTreeFontSizeLabel.text = roundedValue.toString()
            }
        }

        recordingTreeFontSpacingLabel.text = currentFontSettings.recordingTreeFontSpacing.toString()
        recordingTreeFontSpacingSlider.value = currentFontSettings.recordingTreeFontSpacing.toDouble()
        recordingTreeFontSpacingSlider.valueProperty().addListener {_, _, newValue ->
            settingStorage.updateSettings { settings ->
                settings.appearanceSettings.fontSettings.recordingTreeFontSpacing = newValue.toInt()
                val roundedValue = settings.appearanceSettings.fontSettings.recordingTreeFontSpacing
                fontSizeUpdater.update(UIApplication.stage.scene, settings.appearanceSettings.fontSettings)
                recordingTreeFontSpacingLabel.text = roundedValue.toString()
            }
        }
    }
}