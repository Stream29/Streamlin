package io.github.stream29.streamlin.serialize.transform

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.JvmName

/**
 * A transformer that transforms [Serializable] objects, [List] and [Map] to and from [Value] objects.
 *
 * @param serializersModule The serializers module to use for encoding and decoding.
 * @param configuration The configuration to use for encoding and decoding.
 */
open class Transformer(
    override val serializersModule: SerializersModule,
    val configuration: TransformConfiguration
) : SerialFormat {

    /**
     * Creates a transformer with the empty serializers module and given configuration.
     *
     * @param block The configuration block to apply to the transformer.
     */
    constructor(block: TransformConfiguration.() -> Unit) : this(
        EmptySerializersModule(),
        TransformConfiguration().apply(block)
    )

    /**
     * The default transformer with the empty serializers module and default configuration.
     */
    companion object Default : Transformer(EmptySerializersModule(), TransformConfiguration())

    /**
     * Encodes a [Serializable] object to a [Value] object.
     *
     * @param T The type of the object to encode.
     * @param serializer The serializer to use for encoding.
     * @param value The object to encode.
     * @return The encoded value.
     */
    fun <T> encodeToValue(serializer: SerializationStrategy<T>, value: T) =
        AnyEncoder(serializersModule, configuration).also { serializer.serialize(it, value) }.record

    /**
     * Encodes a [Serializable] object to a [Value] object.
     *
     * @param T The type of the object to encode.
     * @param value The object to encode.
     * @return The encoded value.
     */
    inline fun <reified T> fromSerializable(value: T) = encodeToValue(serializersModule.serializer(), value)

    /**
     * Encodes a [List] to a [Value] object.
     *
     * Embedded generic structure is not supported because of type erase.
     *
     * @param list The [List] to encode.
     * @return The encoded value.
     */
    fun fromList(list: List<*>) = StructureValue().also {
        list.forEachIndexed { index, element ->
            it.add(Property(index, fromAny(element)))
        }
    }

    /**
     * Encodes a [Map] to a [Value] object.
     *
     * Embedded generic structure is not supported because of type erase.
     *
     * @param map The [Map] to encode.
     * @return The encoded value.
     */
    fun fromMap(map: Map<*, *>): StructureValue =
        StructureValue().also {
            map.forEach { (key, value) ->
                it.add(Property(key, fromAny(value)))
            }
        }

    /**
     * Encodes any value to a [Value] object.
     *
     * Generic type is not supported because of type erase.
     *
     * @param value The value to encode.
     * @return The encoded value.
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun fromAny(value: Any?): Value =
        when (value) {
            null -> PrimitiveValue(null)
            is String -> PrimitiveValue(value)
            is Int -> PrimitiveValue(value)
            is Long -> PrimitiveValue(value)
            is Float -> PrimitiveValue(value)
            is Double -> PrimitiveValue(value)
            is Boolean -> PrimitiveValue(value)
            is Map<*, *> -> fromMap(value)
            is List<*> -> fromList(value)
            else -> {
                val serializer = serializersModule.serializer(
                    value::class,
                    emptyList(),
                    false
                )
                encodeToValue(serializer, value)
            }
        }

    /**
     * Decodes a [Value] object to a [Serializable] object.
     *
     * @param T The type of the object to decode.
     * @param deserializer The deserializer to use for decoding.
     * @param value The value to decode.
     */
    inline fun <reified T> decodeFromValue(
        deserializer: DeserializationStrategy<T>,
        value: Value
    ) = deserializer.deserialize(AnyDecoder(serializersModule, value))

    /**
     * Decodes a [Value] object to a [Serializable] object with serializer from [serializersModule].
     *
     * @param T The type of the object to decode.
     * @param value The value to decode.
     */
    inline fun <reified T> decodeFromValue(
        value: Value
    ) = decodeFromValue(serializersModule.serializer<T>(), value)

    /**
     * Decodes a [Value] object to a [Serializable] object.
     *
     * @param T The type of the object to decode.
     * @param value The value to decode.
     */
    inline fun <reified T> toSerializable(value: Value): T =
        decodeFromValue(value)

    /**
     * Decodes a [Value] object to a [Serializable] object.
     *
     * @param T The type of the object to decode.
     * @receiver The value to decode.
     */
    @JvmName("contextToSerializable")
    inline fun <reified T> Value.toSerializable(): T =
        toSerializable<T>(this)

    /**
     * Decodes a [Value] object to a [Map].
     *
     * @param value The value to decode.
     */
    fun toMap(value: Value): Map<*, *> =
        value.let {
            it as? StructureValue
                ?: throw SerializationException("Only StructureValue can be converted to Map")
        }.let { toMap(it) }

    /**
     * Decodes a [Value] object to a [Map].
     *
     * @receiver The value to decode.
     */
    @JvmName("contextToMap")
    fun Value.toMap(): Map<*, *> =
        toMap(this)

    /**
     * Decodes a [StructureValue] object to a [Map].
     *
     * @param value The value to decode.
     */
    fun toMap(value: StructureValue): Map<*, *> =
        value.associate {
            val value = it.value
            val key = it.key
            when (value) {
                is PrimitiveValue -> key.value to value.value
                is StructureValue -> key.value to toMap(value)
            }
        }

    /**
     * Decodes a [StructureValue] object to a [Map].
     *
     * @receiver The value to decode.
     */
    @JvmName("contextToMap")
    fun StructureValue.toMap(): Map<*, *> =
        toMap(this)
}