package com.ulyp.ui

import com.ulyp.ui.looknfeel.ThemeManager
import javafx.scene.Parent
import javafx.scene.Scene
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.ref.WeakReference

/**
 * Keeps all currently shown scenes in a single place
 */
@Component
open class SceneRegistry(@Autowired private val themeManager: ThemeManager) {

    private var scenes: MutableSet<WeakReference<Scene>> = mutableSetOf()

    fun newScene(parent: Parent): Scene {
        val scene = Scene(parent)

        scenes.add(WeakReference(scene))

        scene.stylesheets.addAll(themeManager.currentTheme.cssPaths)
        return scene
    }

    private fun cleanup() {
        val toRemove = mutableSetOf<WeakReference<Scene>>()

        scenes.forEach {
            if (it.get() == null) {
                toRemove.add(it)
            }
        }

        scenes.removeAll(toRemove)
    }

    fun scenes(): List<Scene> {
        cleanup()
        return scenes.mapNotNull { it.get() }.toList()
    }
}