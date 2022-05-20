package com.ulyp.ui.looknfeel

enum class Theme(val ulypCssPath: List<String>, val rsyntaxThemePath: String) {

    LIGHT(listOf("themes/ulyp-base.css", "themes/light/ulyp-light.css"), "/themes/light/rsyntax-light.xml"),
    DARK(listOf("themes/ulyp-base.css", "themes/dark/ulyp-dark.css"), "/themes/dark/rsyntax-dark.xml");

    fun nextTheme(): Theme {
        val nextOrdinal = (this.ordinal + 1) % values().size;
        return values()[nextOrdinal]
    }
}