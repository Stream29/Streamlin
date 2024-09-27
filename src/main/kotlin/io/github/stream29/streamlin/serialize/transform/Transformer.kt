package io.github.stream29.streamlin.serialize.transform

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
open class Transformer(
    override val serializersModule: SerializersModule,
    val configuration: TransformConfiguration
) : SerialFormat {
    constructor(block: TransformConfiguration.() -> Unit) : this(
        EmptySerializersModule(),
        TransformConfiguration().apply(block)
    )

    companion object Default : Transformer(EmptySerializersModule(), TransformConfiguration())

    inline fun <reified T> decodeFromValue(value: Value) = decodeFromValue(serializer<T>(), value)

    inline fun <reified T> encodeToValue(value: T) = encodeToValue(serializer<T>(), value)

    inline fun <reified T> decodeFromValue(
        deserializer: DeserializationStrategy<T>,
        value: Value
    ) = deserializer.deserialize(AnyDecoder(serializersModule, value))

    fun <T> encodeToValue(serializer: SerializationStrategy<T>, value: T) =
        AnyEncoder(serializersModule, configuration).also { serializer.serialize(it, value) }.record
}