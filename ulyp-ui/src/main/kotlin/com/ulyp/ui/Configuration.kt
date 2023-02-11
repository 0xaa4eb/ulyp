package com.ulyp.ui

import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.looknfeel.FontSizeChanger
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.reader.FilterRegistry
import com.ulyp.ui.settings.SettingsStorage
import com.ulyp.ui.settings.defaults.SettingsFileProvider
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@ComponentScan(value = ["com.ulyp.ui"])
open class Configuration {

    @Bean
    open fun stage(): Stage {
        return UIApplication.stage
    }

    @Bean
    open fun settingsStorage(settingsFileProvider: SettingsFileProvider): SettingsStorage {
        return SettingsStorage(settingsFileProvider.getSettingsFile())
    }

    @Bean
    @Lazy
    open fun primaryView(
            applicationContext: ApplicationContext,
            filterRegistry: FilterRegistry,
            sourceCodeView: SourceCodeView,
            fileRecordingTabPane: FileRecordingTabPane,
            settingsStorage: SettingsStorage,
            fontSizeChanger: FontSizeChanger,
            stage: Stage
    ): PrimaryView {
        val fileChooser = FileChooser()

        return PrimaryView(
                applicationContext,
                filterRegistry,
                sourceCodeView,
                fileRecordingTabPane,
                settingsStorage,
                fontSizeChanger
        ) { fileChooser.showOpenDialog(stage) }
    }

    @Bean
    @Lazy
    open fun settingsView(
            applicationContext: ApplicationContext,
            themeManager: ThemeManager,
            fontSizeChanger: FontSizeChanger,
            stage: Stage,
            settingStorage: SettingsStorage
    ): SettingsView {
        return SettingsView(themeManager, fontSizeChanger, settingStorage)
    }

    @Bean
    @Lazy
    open fun filterView(
        applicationContext: ApplicationContext,
        stage: Stage,
        filterRegistry: FilterRegistry
    ): FilterView {
        return FilterView(filterRegistry)
    }

    @Bean
    @Lazy
    open fun fileRecordingTabPane(): FileRecordingTabPane {
        val tabPane = FileRecordingTabPane()

        tabPane.prefHeight = 408.0
        tabPane.prefWidth = 354.0
        return tabPane
    }
}