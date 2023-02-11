package com.ulyp.ui.settings

import kotlinx.serialization.Serializable

@Serializable
class AppearanceSettings {

    companion object {
        fun default(): AppearanceSettings {
            return AppearanceSettings()
        }
    }

    var fontSettings: FontSettings = FontSettings.default()
}