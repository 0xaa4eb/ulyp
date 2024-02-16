package com.ulyp.ui.looknfeel

import com.ulyp.ui.SceneRegistry
import org.springframework.stereotype.Service

@Service
class ThemeManager() {

    private lateinit var sceneRegistry: SceneRegistry

    var currentTheme: Theme = Theme.DARK

    fun changeTheme(theme: Theme) {

        sceneRegistry.scenes().forEach { scene ->
            scene.stylesheets.removeIf {
                it.startsWith("themes/")
                        && it.endsWith("css")
                        && it.contains("ulyp", ignoreCase = true)
            }

            scene.stylesheets.removeIf { it.contains("ulyp") }
            scene.stylesheets.addAll(0, theme.cssPaths)
        }

        this.currentTheme = theme
    }

    fun setSceneRegistry(value: SceneRegistry) {
        this.sceneRegistry = value
    }
}