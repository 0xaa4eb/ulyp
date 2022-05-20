package com.ulyp.ui

import com.ulyp.ui.looknfeel.ThemeManager
import javafx.scene.Parent
import javafx.scene.Scene
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Keeps all currently shown scenes in a single place
 */
@Component
open class SceneRegistry(@Autowired private val themeManager: ThemeManager) {

    private var scenes: MutableSet<Scene> = mutableSetOf()

    fun newScene(parent: Parent): Scene {
        val scene = Scene(parent)
        scenes.add(scene)

        scene.stylesheets.addAll(themeManager.currentTheme.cssPaths)
        return scene
    }

    fun removeScene(scene: Scene) {
        scenes.remove(scene)
    }

    fun scenes(): Collection<Scene> {
        return scenes
    }
}