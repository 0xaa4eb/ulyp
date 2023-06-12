package com.ulyp.ui.settings

import com.ulyp.ui.settings.serializer.BooleanPropertySerializer
import com.ulyp.ui.settings.serializer.IntegerPropertySerializer
import com.ulyp.ui.settings.serializer.StringPropertySerializer
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.scene.text.Font
import kotlinx.serialization.Serializable

@Serializable
class Settings {

    @Serializable(with = IntegerPropertySerializer::class)
    val systemFontSize = SimpleIntegerProperty(13)
    @Serializable(with = IntegerPropertySerializer::class)
    val recordingTreeFontSize = SimpleIntegerProperty(15)
    @Serializable(with = IntegerPropertySerializer::class)
    val recordingTreeFontSpacing = SimpleIntegerProperty(8)
    @Serializable(with = StringPropertySerializer::class)
    val recordingTreeFontName: StringProperty = SimpleStringProperty(Font.getDefault().name)
    @Serializable(with = BooleanPropertySerializer::class)
    val recordingListShowThreads: BooleanProperty = SimpleBooleanProperty(true)

    fun addListener(listener: ChangeListener<Any>) {
        systemFontSize.addListener(listener)
        recordingTreeFontSize.addListener(listener)
        recordingTreeFontSpacing.addListener(listener)
        recordingTreeFontName.addListener(listener)
        recordingListShowThreads.addListener(listener)
    }
}