package com.ulyp.ui

import com.ulyp.ui.elements.recording.tree.FileRecordingTabPane
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.looknfeel.ThemeManager
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(value = ["com.ulyp.ui"])
open class Configuration {

    @Bean
    open fun stage(): Stage {
        return Main.stage
    }

    @Bean
    @Lazy
    open fun viewController(
            applicationContext: ApplicationContext,
            sourceCodeView: SourceCodeView,
            processTabPane: FileRecordingTabPane,
            themeManager: ThemeManager,
            stage: Stage
    ): PrimaryViewController {
        val fileChooser = FileChooser()

        return PrimaryViewController(
                applicationContext,
                sourceCodeView,
                processTabPane,
                themeManager,
                { fileChooser.showOpenDialog(stage) }
        )
    }

    @Bean
    @Lazy
    open fun processTabPane(): FileRecordingTabPane {
        val tabPane = FileRecordingTabPane()

        tabPane.prefHeight = 408.0
        tabPane.prefWidth = 354.0
        return tabPane
    }
}