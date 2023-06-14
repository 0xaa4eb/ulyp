package com.ulyp.ui

import com.ulyp.ui.looknfeel.FontSizeUpdater
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
    private lateinit var themeManager: ThemeManager
    @Autowired
    private lateinit var fontSizeUpdater: FontSizeUpdater

    fun init() {
        settings.systemFontName.addListener { _, _, _ ->
            fontSizeUpdater.update(UIApplication.stage.scene, settings)
        }
        settings.systemFontSize.addListener { _, _, _ ->
            fontSizeUpdater.update(UIApplication.stage.scene, settings)
        }
        settings.recordingTreeFontSpacing.addListener { _, _, _ ->
            fontSizeUpdater.update(UIApplication.stage.scene, settings)
        }
        settings.recordingTreeFontName.addListener { _, _, _ ->
            fontSizeUpdater.update(UIApplication.stage.scene, settings)
        }
        settings.recordingTreeFontSize.addListener { _, _, _ ->
            fontSizeUpdater.update(UIApplication.stage.scene, settings)
        }

        Platform.runLater {
            fontSizeUpdater.update(UIApplication.stage.scene, settings)
        }

        themeManager.changeTheme(Theme.valueOf(settings.theme.get()))
        settings.theme.addListener { _, _, theme ->
            themeManager.changeTheme(Theme.valueOf(theme))
        }
    }
}