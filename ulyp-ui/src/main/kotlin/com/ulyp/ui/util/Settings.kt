package com.ulyp.ui.util

import com.ulyp.ui.UIApplication
import com.ulyp.ui.looknfeel.DefaultFontNameResolver
import com.ulyp.ui.looknfeel.FontSizeChanger
import org.springframework.stereotype.Component
import kotlin.math.roundToInt

@Component
class Settings(
        defaultFontNameResolver: DefaultFontNameResolver,
        private val fontSizeChanger: FontSizeChanger) {

    var fontName: String = defaultFontNameResolver.resolve()
        set(value) {
            fontSizeChanger.refresh(UIApplication.stage.scene, recordingTreeFontSize, value)
            field = value
        }

    var recordingTreeFontSize: Double = 1.0
        set(value) {
            val valueRounded = (value * 20.0).roundToInt() * 1.0 / 20.0
            fontSizeChanger.refresh(UIApplication.stage.scene, valueRounded, fontName)
            field = valueRounded
        }

    fun increaseFont() {
        recordingTreeFontSize += 0.05
    }

    fun decreaseFont() {
        recordingTreeFontSize -= 0.05
    }
}