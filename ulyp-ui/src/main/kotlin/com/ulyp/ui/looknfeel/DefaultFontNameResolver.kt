package com.ulyp.ui.looknfeel

import javafx.scene.text.Font
import org.springframework.stereotype.Component

@Component
class DefaultFontNameResolver {

    private val fontName: String

    init {
        val families = Font.getFontNames()
        var chosenFontName: String = Font.getDefault().name
        if (families.contains("Monospaced")) {
            chosenFontName = "Monospaced"
        }
        if (families.contains("Consolas")) {
            chosenFontName = "Consolas"
        }

        fontName = chosenFontName
    }

    fun resolve(): String {
        return fontName
    }
}