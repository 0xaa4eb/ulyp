package com.ulyp.ui.util

import com.ulyp.ui.Main
import com.ulyp.ui.looknfeel.DefaultFontNameResolver
import com.ulyp.ui.looknfeel.FontSizeChanger
import org.springframework.stereotype.Component

@Component
class Settings(
        defaultFontNameResolver: DefaultFontNameResolver,
        private val fontSizeChanger: FontSizeChanger) {

    var fontName: String = defaultFontNameResolver.resolve()
        get() = field
        set(value) {
            fontSizeChanger.refresh(Main.stage.scene, currentFontSize, value)
            field = value
        }

    var currentFontSize = 1.0
        set(value) {
            fontSizeChanger.refresh(Main.stage.scene, value, fontName)
            field = value
        }

    fun increaseFont() {
        currentFontSize += 0.05
    }

    fun decreaseFont() {
        currentFontSize -= 0.05
    }
}