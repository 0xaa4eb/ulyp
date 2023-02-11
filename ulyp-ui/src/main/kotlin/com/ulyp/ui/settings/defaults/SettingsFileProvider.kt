package com.ulyp.ui.settings.defaults

import org.springframework.stereotype.Component
import java.io.File

@Component
class SettingsFileProvider {

    fun getSettingsFile(): File {
        return File("./settings.json")
    }
}