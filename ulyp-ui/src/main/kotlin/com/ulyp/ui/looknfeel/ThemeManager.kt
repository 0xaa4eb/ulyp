package com.ulyp.ui.looknfeel

import com.ulyp.ui.code.SourceCodeTab
import javafx.scene.Scene
import org.springframework.stereotype.Service

@Service
class ThemeManager(private val sourceCodeTab: SourceCodeTab) {

    private var theme: Theme? = null

    fun applyTheme(theme: Theme, scene: Scene) {

        scene.stylesheets.removeIf {
            it.startsWith("themes/")
            && it.endsWith("css")
            && it.contains("ulyp", ignoreCase = true)
        }

        scene.stylesheets.add(theme.ulypCssPath)
        this.sourceCodeTab.setTheme(theme.rsyntaxThemePath)
        this.theme = theme
    }
}