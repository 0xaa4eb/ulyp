package com.ulyp.ui.looknfeel

import com.ulyp.ui.SceneRegistry
import com.ulyp.ui.code.SourceCodeTab
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class ThemeManager(@Autowired private val sourceCodeTab: SourceCodeTab) {

    private lateinit var sceneRegistry: SceneRegistry

    var currentTheme: Theme = Theme.DARK
        get() = field

    fun applyTheme(theme: Theme) {

        sceneRegistry.scenes().forEach { scene ->
            scene.stylesheets.removeIf {
                it.startsWith("themes/")
                        && it.endsWith("css")
                        && it.contains("ulyp", ignoreCase = true)
            }

            scene.stylesheets.removeIf { it.contains("ulyp") }
            scene.stylesheets.addAll(0, theme.cssPaths)
        }

        this.sourceCodeTab.setTheme(theme.rsyntaxThemePath)
        this.currentTheme = theme
    }

    fun setSceneRegistry(value: SceneRegistry) {
        this.sceneRegistry = value
    }
}