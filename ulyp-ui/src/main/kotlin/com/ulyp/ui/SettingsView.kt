package com.ulyp.ui

import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ChoiceBox
import org.springframework.context.ApplicationContext
import java.net.URL
import java.util.*


class SettingsView(
        private val applicationContext: ApplicationContext,
        private val themeManager: ThemeManager
) : Initializable {

    @FXML
    lateinit var settingsChoiceBox: ChoiceBox<String>

    override fun initialize(url: URL, rb: ResourceBundle?) {
        settingsChoiceBox.items.addAll(Theme.values().map { it.name }.toList())
        settingsChoiceBox.selectionModel.select(themeManager.currentTheme.name)
        settingsChoiceBox.setOnAction {
            val selectedTheme: String = settingsChoiceBox.selectionModel.selectedItem
            themeManager.applyTheme(Theme.valueOf(selectedTheme))
        }
    }
}