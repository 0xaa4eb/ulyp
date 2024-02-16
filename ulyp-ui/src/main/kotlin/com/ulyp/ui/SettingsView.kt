package com.ulyp.ui

import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.settings.Settings
import com.ulyp.ui.settings.SimpleIntegerProperty
import com.ulyp.ui.util.connect
import javafx.beans.property.StringProperty
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
    @FXML
    lateinit var recordingTreeBoldElements: CheckBox
    @FXML
    lateinit var recordingListSpacingSlider: Slider
    @FXML
    lateinit var recordingListSpacingLabel: Label

    override fun initialize(url: URL, rb: ResourceBundle?) {

        themeChoiceBox.items.addAll(Theme.values().map { it.name }.toList())
        themeChoiceBox.connect(settings.theme)

        systemFontChoiceBox.items.addAll(Font.getFamilies())
        systemFontChoiceBox.connect(settings.systemFontName)

        recordingTreeFontChoiceBox.items.addAll(Font.getFamilies())
        recordingTreeFontChoiceBox.connect(settings.recordingTreeFontName)

        systemFontSizeSlider.connect(systemFontSizeLabel, settings.systemFontSize)
        recordingTreeFontSizeSlider.connect(recordingTreeFontSizeLabel, settings.recordingTreeFontSize)
        recordingTreeFontSpacingSlider.connect(recordingTreeFontSpacingLabel, settings.recordingTreeFontSpacing)
        recordingListSpacingSlider.connect(recordingListSpacingLabel, settings.recordingListSpacing)

        recordingListShowThreads.selectedProperty().bindBidirectional(settings.recordingListShowThreads)
        recordingTreeBoldElements.selectedProperty().bindBidirectional(settings.recordingTreeBoldElements)
    }
}