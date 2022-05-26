package com.ulyp.ui

import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.util.Settings
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
        return Main.stage
    }

    @Bean
    @Lazy
    open fun primaryView(
            applicationContext: ApplicationContext,
            sourceCodeView: SourceCodeView,
            fileRecordingTabPane: FileRecordingTabPane,
            stage: Stage
    ): PrimaryView {
        val fileChooser = FileChooser()

        return PrimaryView(
                applicationContext,
                sourceCodeView,
                fileRecordingTabPane
        ) { fileChooser.showOpenDialog(stage) }
    }

    @Bean
    @Lazy
    open fun settingsView(
            applicationContext: ApplicationContext,
            themeManager: ThemeManager,
            stage: Stage,
            settings: Settings
    ): SettingsView {
        return SettingsView(themeManager, settings)
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