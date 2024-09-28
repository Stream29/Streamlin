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

    inline fun <reified T, reified R> T.transformTo() = transform<T, R>(this)

    inline fun <reified T, reified R> transform(from: T) =
        decodeFromValue<R>(encodeToValue<T>(from))

    inline fun <reified T> encodeToValue(value: T) = encodeToValue(serializer<T>(), value)

    fun <T> encodeToValue(serializer: SerializationStrategy<T>, value: T) =
        AnyEncoder(serializersModule, configuration).also { serializer.serialize(it, value) }.record

    fun encodeAny(list: List<*>) = StructureValue().apply {
        list.forEachIndexed { index, element ->
            add(Property(index, encodeAny(element)))
        }
    }

    fun encodeAny(map: Map<*, *>) = StructureValue().apply {
        map.forEach { (key, value) ->
            add(Property(key, encodeAny(value)))
        }
    }

    fun encodeAny(value: Any?): Value =
        when (value) {
            null -> PrimitiveValue(null)
            is String -> PrimitiveValue(value)
            is Int -> PrimitiveValue(value)
            is Long -> PrimitiveValue(value)
            is Float -> PrimitiveValue(value)
            is Double -> PrimitiveValue(value)
            is Boolean -> PrimitiveValue(value)
            is Map<*, *> -> encodeAny(value)
            is List<*> -> encodeAny(value)
            else -> value::class.java.declaredFields
                .asSequence()
                .filter { !it.isSynthetic }
                .map { it.isAccessible = true; it }
                .map { Property(it.name, encodeAny(it.get(value))) }
                .toMutableList()
                .let { StructureValue(it) }
        }

    inline fun <reified T> decodeFromValue(value: Value) = decodeFromValue(serializer<T>(), value)

    inline fun <reified T> decodeFromValue(
        deserializer: DeserializationStrategy<T>,
        value: Value
    ) = deserializer.deserialize(AnyDecoder(serializersModule, value))

    @Suppress("name_shadowed")
    fun decodeToMap(value: StructureValue): Map<*, *> = value.associate {
        val value = it.value
        val key = it.key
        when (value) {
            is PrimitiveValue -> key.value to value.value
            is StructureValue -> key.value to decodeToMap(value)
        }
    }
}