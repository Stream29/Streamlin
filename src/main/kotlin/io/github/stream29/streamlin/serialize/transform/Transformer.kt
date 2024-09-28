package io.github.stream29.streamlin.serialize.transform

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.descriptors.*

/**
 * A transformer that transforms [Serializable] objects, [List] and [Map] to and from [Value] objects.
 *
 * @param serializersModule The serializers module to use for encoding and decoding.
 * @param configuration The configuration to use for encoding and decoding.
 */
@ExperimentalSerializationApi
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
     * Transforms a [Serializable] object, [List] or [Map] to another [Serializable] object.
     *
     * Transformed without reflection.
     *
     * The `receiver` object must have at least the same properties or more.
     *
     * @param T The type of the object to transform from.
     * @param R The type of the object to transform to.
     * @receiver The object to transform from.
     * @return The transformed object.
     */
    inline fun <reified T, reified R> T.transformTo() = transform<T, R>(this)

    /**
     * Transforms a [Serializable] object, [List] or [Map] to a [Map].
     *
     * Transformed without reflection.
     *
     * The `receiver` object must be [StructureKind].
     *
     * @param T The type of the object to transform from.
     * @receiver The object to transform from.
     * @return The transformed object.
     */
    @JvmName("transformToMapReceiving")
    inline fun <reified T> T.transformToMap(): Map<*, *> = transformToMap(this)

    /**
     * Transforms a [Serializable] object to another s[Serializable] object.
     *
     * Transformed without reflection.
     *
     * The [from] object must have at least the same properties or more.
     *
     * @param T The type of the object to transform from.
     * @param R The type of the object to transform to.
     * @param from The object to transform from.
     * @return The transformed object.
     */
    inline fun <reified T, reified R> transform(from: T) =
        decodeFromValue<R>(encodeToValue<T>(from))

    /**
     * Transforms a [Map] to a [Serializable] object.
     *
     * Transformed without reflection.
     *
     * The [from] map must have at least the same properties or more.
     *
     * @param T The type of the object to transform to.
     * @param from The map to transform from.
     * @return The transformed object.
     */
    inline fun <reified T> transform(from: Map<*, *>) =
        decodeFromValue<T>(encodeAny(from))

    /**
     * Transforms a [List] to a [Serializable] object.
     *
     * Transformed without reflection.
     *
     * The [T] must be a [List] type and compatible with the [from] list.
     *
     * @param T The type of the object to transform to.
     * @param from The [List] to transform from.
     * @return The transformed object.
     */
    inline fun <reified T> transform(from: List<*>) =
        decodeFromValue<T>(encodeAny(from))

    /**
     * Transforms a [Serializable] object, [List] or [Map] to a [Map].
     *
     * Transformed without reflection.
     *
     * The [from] object must be [StructureKind].
     *
     * @param T The type of the object to transform from.
     * @param from The object to transform from.
     * @return The transformed map.
     */
    inline fun <reified T> transformToMap(from: T): Map<*, *> =
        decodeToMap(encodeToValue<T>(from) as StructureValue)

    /**
     * Encodes a [Serializable] object to a [Value] object.
     *
     * @param T The type of the object to encode.
     * @param value The object to encode.
     * @return The encoded value.
     */
    inline fun <reified T> encodeToValue(value: T) = encodeToValue(serializer<T>(), value)

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
     * Encodes a [List] to a [Value] object.
     *
     * May use reflection to encode the list.
     *
     * @param list The [List] to encode.
     * @return The encoded value.
     */
    fun encodeAny(list: List<*>) = StructureValue().apply {
        list.forEachIndexed { index, element ->
            add(Property(index, encodeAny(element)))
        }
    }

    /**
     * Encodes a [Map] to a [Value] object.
     *
     * May use reflection to encode the map.
     *
     * @param map The [Map] to encode.
     * @return The encoded value.
     */
    fun encodeAny(map: Map<*, *>) = StructureValue().apply {
        map.forEach { (key, value) ->
            add(Property(key, encodeAny(value)))
        }
    }

    /**
     * Encodes any value to a [Value] object.
     *
     * May use reflection to encode the value.
     *
     * @param value The value to encode.
     * @return The encoded value.
     */
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

    /**
     * Decodes a [Value] object to a [Serializable] object.
     *
     * @param T The type of the object to decode.
     * @param value The value to decode.
     */
    inline fun <reified T> decodeFromValue(value: Value) = decodeFromValue(serializer<T>(), value)

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
     * Decodes a [Value] object to a [Map].
     *
     * @param value The value to decode.
     */
    fun decodeToMap(value: StructureValue): Map<*, *> = value.associate {
        val value = it.value
        val key = it.key
        when (value) {
            is PrimitiveValue -> key.value to value.value
            is StructureValue -> key.value to decodeToMap(value)
        }
    }
}