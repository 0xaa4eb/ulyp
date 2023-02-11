package com.ulyp.ui.settings

import kotlinx.serialization.Serializable

@Serializable
class Settings {

    var appearanceSettings: AppearanceSettings = AppearanceSettings.default()
}