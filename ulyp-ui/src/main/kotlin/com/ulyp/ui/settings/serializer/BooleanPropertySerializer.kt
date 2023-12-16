package com.ulyp.ui.settings.serializer

import com.ulyp.ui.settings.props.BooleanProperty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BooleanPropertySerializer : KSerializer<BooleanProperty> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BooleanProperty", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: BooleanProperty) {
        encoder.encodeBoolean(value.value)
    }

    override fun deserialize(decoder: Decoder): BooleanProperty {
        return BooleanProperty(decoder.decodeBoolean())
    }
}