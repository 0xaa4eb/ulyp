package com.ulyp.ui

import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.looknfeel.FontSizeUpdater
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.reader.FilterRegistry
import com.ulyp.ui.settings.Settings
import com.ulyp.ui.settings.SettingsFileStorage
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
    open fun settingsStorage(settingsFileProvider: SettingsFileProvider): SettingsFileStorage {
        return SettingsFileStorage(settingsFileProvider.getSettingsFile())
    }

    @Bean
    open fun settings(settingsStorage: SettingsFileStorage): Settings {
        return settingsStorage.read()
    }

    @Bean
    @Lazy
    open fun primaryView(
        applicationContext: ApplicationContext,
        filterRegistry: FilterRegistry,
        sourceCodeView: SourceCodeView,
        fileRecordingTabPane: FileRecordingTabPane,
        settings: Settings,
        fontSizeUpdater: FontSizeUpdater,
        stage: Stage,
        viewInitializer: ViewInitializer
    ): PrimaryView {
        val fileChooser = FileChooser()

        return PrimaryView(
            applicationContext,
            viewInitializer,
            filterRegistry,
            sourceCodeView,
            fileRecordingTabPane,
            settings
        ) { fileChooser.showOpenDialog(stage) }
    }

    @Bean
    @Lazy
    open fun settingsView(
        applicationContext: ApplicationContext,
        settings: Settings,
        themeManager: ThemeManager
    ): SettingsView {
        return SettingsView(settings, themeManager)
    }

    @Bean
    @Lazy
    open fun filterView(
        applicationContext: ApplicationContext,
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