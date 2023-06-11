package com.ulyp.ui.settings.serializer

import com.ulyp.ui.settings.SimpleIntegerProperty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntegerPropertySerializer : KSerializer<SimpleIntegerProperty> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntegerProperty", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: SimpleIntegerProperty) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): SimpleIntegerProperty {
        return SimpleIntegerProperty(decoder.decodeInt())
    }
}