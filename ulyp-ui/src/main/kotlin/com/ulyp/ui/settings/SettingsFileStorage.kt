package com.ulyp.ui.settings

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class SettingsFileStorage(private val settingsFile: File) {

    fun read(): Settings {
        val settings= if (settingsFile.exists()) {
            val settingsJson = settingsFile.readText(Charsets.UTF_8)
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString(settingsJson)
        } else {
            val settings = Settings()
            this.writeToFile(settings)
            settings
        }
        settings.addListener {
                _, _, _ -> writeToFile(settings)
        }
        return settings
    }

    private fun writeToFile(settings: Settings) {
        val encodeToString = Json.encodeToString(settings)
        settingsFile.writeText(encodeToString, Charsets.UTF_8)

    }
}