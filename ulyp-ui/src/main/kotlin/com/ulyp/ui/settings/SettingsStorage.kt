package com.ulyp.ui.settings

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.function.Consumer

class SettingsStorage(private val settingsFile: File) {

    fun updateSettings(updater: Consumer<Settings>) {
        val settings = this.read()
        updater.accept(settings)
        this.write(settings)
    }

    fun read(): Settings {
        return if (settingsFile.exists()) {
            val settingsJson = settingsFile.readText(Charsets.UTF_8)
            Json.decodeFromString(settingsJson)
        } else {
            val settings = Settings()
            this.write(settings)
            settings
        }
    }

    fun write(settings: Settings) {
        val encodeToString = Json.encodeToString(settings)
        settingsFile.writeText(encodeToString, Charsets.UTF_8)
    }
}