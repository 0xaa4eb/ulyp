package com.ulyp.ui.settings

import javafx.scene.text.Font
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
class FontSettings {

    companion object {
        fun default(): FontSettings {
            return FontSettings()
        }
    }

    var systemFontSize: Double = 1.0
        set(value) {
            val valueRounded = (value * 50.0).roundToInt() * 1.0 / 50.0
            field = valueRounded
        }
    var recordingTreeFontSize: Double = 1.0
        set(value) {
            val valueRounded = (value * 50.0).roundToInt() * 1.0 / 50.0
            field = valueRounded
        }
    var recordingTreeFontName: String? = Font.getDefault().name
}