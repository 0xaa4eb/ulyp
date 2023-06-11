package com.ulyp.ui.settings.serializer

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringPropertySerializer : KSerializer<StringProperty> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringProperty", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StringProperty) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): StringProperty {
        return SimpleStringProperty(decoder.decodeString())
    }
}