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

    var systemFontSize: Int = 15

    var recordingTreeFontSize: Int = 15

    var recordingTreeFontName: String? = Font.getDefault().name
}