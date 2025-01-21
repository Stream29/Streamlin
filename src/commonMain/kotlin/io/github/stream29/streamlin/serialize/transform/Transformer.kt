package io.github.stream29.streamlin.serialize.transform

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * A transformer that transforms [Serializable] objects, [List] and [Map] to and from [Value] objects.
 *
 * @param serializersModule The serializers module to use for encoding and decoding.
 * @param configuration The configuration to use for encoding and decoding.
 */
public open class Transformer(
    override val serializersModule: SerializersModule,
    public val configuration: TransformConfiguration
) : SerialFormat {

    /**
     * Creates a transformer with the empty serializers module and given configuration.
     *
     * @param block The configuration block to apply to the transformer.
     */
    public constructor(block: TransformConfiguration.() -> Unit) : this(
        EmptySerializersModule(),
        TransformConfiguration().apply(block)
    )

    /**
     * The default transformer with the empty serializers module and default configuration.
     */
    public companion object Default : Transformer(EmptySerializersModule(), TransformConfiguration())

    /**
     * Decodes a [Value] object to a [Serializable] object.
     *
     * @param T The type of the object to decode.
     * @receiver The value to decode.
     */
    public inline fun <reified T> Value.toSerializable(): T =
        toSerializable<T>(this)

    /**
     * Decodes a [Value] object to a [Map].
     *
     * @receiver The value to decode.
     */
    public fun Value.toMap(): Map<*, *> =
        toMap(this)

    /**
     * Decodes a [StructureValue] object to a [Map].
     *
     * @receiver The value to decode.
     */
    public fun StructureValue.toMap(): Map<*, *> =
        toMap(this)
}

/**
 * Encodes a [Serializable] object to a [Value] object.
 *
 * @param T The type of the object to encode.
 * @param serializer The serializer to use for encoding.
 * @param value The object to encode.
 * @return The encoded value.
 */
public fun <T> Transformer.encodeToValue(serializer: SerializationStrategy<T>, value: T) =
    AnyEncoder(serializersModule, configuration).also { serializer.serialize(it, value) }.record

/**
 * Encodes a [Serializable] object to a [Value] object.
 *
 * @param T The type of the object to encode.
 * @param value The object to encode.
 * @return The encoded value.
 */
public inline fun <reified T> Transformer.fromSerializable(value: T) = encodeToValue(serializersModule.serializer(), value)

/**
 * Encodes a [List] to a [Value] object.
 *
 * Embedded generic structure is not supported because of type erase.
 *
 * @param list The [List] to encode.
 * @return The encoded value.
 */
public fun Transformer.fromList(list: List<*>) = StructureValue().also {
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
public fun Transformer.fromMap(map: Map<*, *>): StructureValue =
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
public fun Transformer.fromAny(value: Any?): Value =
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
public inline fun <reified T> Transformer.decodeFromValue(
    deserializer: DeserializationStrategy<T>,
    value: Value
) = deserializer.deserialize(AnyDecoder(serializersModule, value))

/**
 * Decodes a [Value] object to a [Serializable] object with serializer from [serializersModule].
 *
 * @param T The type of the object to decode.
 * @param value The value to decode.
 */
public inline fun <reified T> Transformer.decodeFromValue(
    value: Value
) = decodeFromValue(serializersModule.serializer<T>(), value)

/**
 * Decodes a [Value] object to a [Serializable] object.
 *
 * @param T The type of the object to decode.
 * @param value The value to decode.
 */
public inline fun <reified T> Transformer.toSerializable(value: Value): T =
    decodeFromValue(value)

/**
 * Decodes a [Value] object to a [Map].
 *
 * @param value The value to decode.
 */
public fun Transformer.toMap(value: Value): Map<*, *> =
    value.let {
        it as? StructureValue
            ?: throw SerializationException("Only StructureValue can be converted to Map")
    }.let { toMap(it) }

/**
 * Decodes a [StructureValue] object to a [Map].
 *
 * @param value The value to decode.
 */
public fun Transformer.toMap(value: StructureValue): Map<*, *> =
    value.associate {
        val value = it.value
        val key = it.key
        when (value) {
            is PrimitiveValue -> key.value to value.value
            is StructureValue -> key.value to toMap(value)
        }
    }