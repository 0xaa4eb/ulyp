package com.ulyp.ui.looknfeel

import javafx.scene.text.Font

class FontChooser {

    private val fontName: String

    init {
        // TODO should be configurable from UI
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

    fun getFontName(): String {
        return fontName
    }
}