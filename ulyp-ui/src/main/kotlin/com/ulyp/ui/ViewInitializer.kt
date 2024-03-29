package com.ulyp.ui

import com.ulyp.ui.looknfeel.FontStyleUpdater
import com.ulyp.ui.looknfeel.RecordingListStyleUpdater
import com.ulyp.ui.looknfeel.Theme
import com.ulyp.ui.looknfeel.ThemeManager
import com.ulyp.ui.settings.Settings
import javafx.application.Platform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ViewInitializer {

    @Autowired
    private lateinit var settings: Settings
    @Autowired
    private lateinit var sceneRegistry: SceneRegistry
    @Autowired
    private lateinit var themeManager: ThemeManager
    @Autowired
    private lateinit var fontStyleUpdater: FontStyleUpdater
    @Autowired
    private lateinit var recordingListStyleUpdater: RecordingListStyleUpdater

    fun init() {
        settings.systemFontName.addListener { _, _, _ ->
            fontStyleUpdater.update(sceneRegistry.scenes(), settings)
        }
        settings.systemFontSize.addListener { _, _, _ ->
            fontStyleUpdater.update(sceneRegistry.scenes(), settings)
        }
        settings.recordingTreeFontSpacing.addListener { _, _, _ ->
            fontStyleUpdater.update(sceneRegistry.scenes(), settings)
        }
        settings.recordingTreeFontName.addListener { _, _, _ ->
            fontStyleUpdater.update(sceneRegistry.scenes(), settings)
        }
        settings.recordingTreeFontSize.addListener { _, _, _ ->
            fontStyleUpdater.update(sceneRegistry.scenes(), settings)
        }
        settings.recordingTreeBoldElements.addListener { _, _, _ ->
            fontStyleUpdater.update(sceneRegistry.scenes(), settings)
        }
        settings.recordingTreeBoldElements.addListener { _, _, _ ->
            fontStyleUpdater.update(sceneRegistry.scenes(), settings)
        }
        settings.recordingListSpacing.addListener { _, _, _ ->
            recordingListStyleUpdater.update(sceneRegistry.scenes(), settings)
        }

        Platform.runLater {
            fontStyleUpdater.update(UIApplication.stage.scene, settings)
            recordingListStyleUpdater.update(sceneRegistry.scenes(), settings)
        }

        themeManager.changeTheme(Theme.valueOf(settings.theme.get()))
        settings.theme.addListener { _, _, theme ->
            themeManager.changeTheme(Theme.valueOf(theme))
        }
    }
}