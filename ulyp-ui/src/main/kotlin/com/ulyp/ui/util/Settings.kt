package com.ulyp.ui.util

import com.ulyp.ui.UIApplication
import com.ulyp.ui.looknfeel.DefaultFontNameResolver
import com.ulyp.ui.looknfeel.FontSettings
import com.ulyp.ui.looknfeel.FontSizeChanger
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class Settings(
        defaultFontNameResolver: DefaultFontNameResolver,
        private val fontSizeChanger: FontSizeChanger) {

    var fontName: String = defaultFontNameResolver.resolve()
        set(value) {
            fontSizeChanger.refresh(
                UIApplication.stage.scene,
                FontSettings(recordingTreeFontSize = recordingTreeFontSize, recordingTreeFontName = value, systemFontSize = 1.0)
            )
            field = value
        }

    var systemFontSize: Double = 1.0
        set(value) {
            val valueRounded = (value * 50.0).roundToInt() * 1.0 / 50.0
            field = valueRounded

            fontSizeChanger.refresh(
                UIApplication.stage.scene,
                FontSettings(recordingTreeFontSize = recordingTreeFontSize, recordingTreeFontName = fontName, systemFontSize = valueRounded)
            )
        }

    var recordingTreeFontSize: Double = 1.0
        set(value) {
            val valueRounded = (value * 50.0).roundToInt() * 1.0 / 50.0
            fontSizeChanger.refresh(
                UIApplication.stage.scene,
                FontSettings(recordingTreeFontSize = valueRounded, recordingTreeFontName = fontName, systemFontSize = 1.0)
            )
            field = valueRounded
        }

    fun increaseRecordingTreeFont() {
        recordingTreeFontSize += 0.02
    }

    fun decreaseRecordingTreeFont() {
        recordingTreeFontSize -= 0.02
    }
}