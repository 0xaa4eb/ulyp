package com.ulyp.ui.looknfeel

enum class Theme(val cssPaths: List<String>) {

    LIGHT(listOf("themes/ulyp-base.css", "themes/light/ulyp-light.css")),
    DARK(listOf("themes/ulyp-base.css", "themes/dark/ulyp-dark.css"));
}