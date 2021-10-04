package com.ulyp.ui.config

import com.ulyp.ui.Main
import com.ulyp.ui.PrimaryViewController
import com.ulyp.ui.ProcessTabPane
import com.ulyp.ui.code.SourceCodeView
import com.ulyp.ui.looknfeel.FontSizeChanger
import com.ulyp.ui.looknfeel.ThemeManager
import javafx.scene.Scene
import javafx.stage.FileChooser
import javafx.stage.Stage
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
    open fun viewController(
            sourceCodeView: SourceCodeView,
            processTabPane: ProcessTabPane,
            fontSizeChanger: FontSizeChanger,
            themeManager: ThemeManager,
            stage: Stage
    ): PrimaryViewController {
        val fileChooser = FileChooser()

        return PrimaryViewController(
                sourceCodeView,
                processTabPane,
                fontSizeChanger,
                themeManager,
                { fileChooser.showOpenDialog(stage) }
        )
    }

    @Bean
    @Lazy
    open fun processTabPane(): ProcessTabPane {
        val tabPane = ProcessTabPane()

        tabPane.prefHeight = 408.0
        tabPane.prefWidth = 354.0
        return tabPane
    }
}