package com.ulyp.ui.looknfeel

import com.ulyp.ui.code.SourceCodeTab
import javafx.scene.Scene
import org.springframework.stereotype.Service

@Service
class ThemeManager(private val sourceCodeTab: SourceCodeTab) {

    private var currentTheme: Theme = Theme.DARK
        get() = field

    fun switchToNextTheme(scene: Scene) {
        applyTheme(currentTheme.nextTheme(), scene)
    }

    private fun applyTheme(theme: Theme, scene: Scene) {

        scene.stylesheets.removeIf {
            it.startsWith("themes/")
            && it.endsWith("css")
            && it.contains("ulyp", ignoreCase = true)
        }

        scene.stylesheets.removeIf { it.contains("ulyp") }
        scene.stylesheets.addAll(0, theme.ulypCssPath)
        this.sourceCodeTab.setTheme(theme.rsyntaxThemePath)
        this.currentTheme = theme
    }
}