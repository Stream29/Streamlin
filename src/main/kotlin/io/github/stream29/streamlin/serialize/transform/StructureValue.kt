package io.github.stream29.streamlin.serialize.transform

import kotlin.Any
import kotlinx.serialization.descriptors.*

/**
 * A property that represents a [Value] with name.
 *
 * [PrimitiveProperty] stands for [PrimitiveKind] value with name.
 *
 * [StructureProperty] stands for [StructureKind] or [PolymorphicKind] value with name.
 *
 * @property key The key of the property.
 * @property value The value of the property.
 */
sealed interface Property {
    val key: PrimitiveValue
    val value: Value

    companion object {
        /**
         * Creates a [Property] with the given [key] and [value].
         *
         * @param key The key of the property. Only keys of [PrimitiveKind] are allowed.
         * @param value The value of the property. Values of [PrimitiveValue] or [StructureValue] are allowed.
         */
        operator fun invoke(key: Any?, value: Value) =
            when (value) {
                is PrimitiveValue -> PrimitiveProperty.of(key, value)
                is StructureValue -> StructureProperty.of(key, value)
            }
    }

    /**
     * Returns `true` if the value of this property is [PrimitiveProperty] and it contains `null` value.
     */
    fun isNullProperty(): Boolean = value.isNullValue()
}

/**
 * A property that represents [PrimitiveKind] with name.
 *
 * @property key The key of the property.
 * @property value The value of the property.
 */
data class PrimitiveProperty(
    override val key: PrimitiveValue,
    override val value: PrimitiveValue
) : Property {
    companion object {
        /**
         * Creates a [PrimitiveProperty] with the given [key] and [value].
         *
         * @param key The key of the property. Should be [PrimitiveKind].
         * @param value The value of the property. Should be [PrimitiveKind].
         */
        fun of(key: Any?, value: Any?) = PrimitiveProperty(PrimitiveValue(key), PrimitiveValue(value))
    }

    override fun toString() = "Property(${key.value.toClarifyString()}=${value.value.toClarifyString()})"
}

/**
 * A property that represents a [StructureKind] or [PolymorphicKind] with name.
 *
 * @property key The key of the property.
 * @property value The value of the property.
 */
data class StructureProperty(
    override val key: PrimitiveValue,
    override val value: StructureValue
) : Property {
    companion object {
        /**
         * Creates a [StructureProperty] with the given [key] and [value].
         *
         * @param key The key of the property. Should be [PrimitiveKind].
         * @param value The value of the property.
         */
        fun of(key: Any?, value: StructureValue) = StructureProperty(PrimitiveValue(key), value)
    }

    override fun toString() = "Property(key=${key.value.toClarifyString()}, value=$value)"
}

/**
 * A value that represents [StructureKind] or [PolymorphicKind] value.
 * It contains a list of properties.
 *
 * If the structure represents a [StructureKind.OBJECT], the properties are treated as the properties of the object.
 *
 * If the structure represents a [StructureKind.MAP], the properties are treated as key-value pairs.
 *
 * If the structure represents a [StructureKind.LIST], the properties are treated as elements whose key is it's index.
 *
 * If the structure represents a [PolymorphicKind], the properties are treated as the properties of the object,
 * and the first property should be [PrimitiveProperty] of [String] recording the polymorphic object's real type, named "type".
 *
 * @property component The properties of the structure.
 */
@Suppress("MemberVisibilityCanBePrivate")
data class StructureValue(
    val component: MutableList<Property> = mutableListOf()
) : Value, MutableList<Property> by component {
    override fun toString() = "Structure(component=$component)"
}

/**
 * A value that represents [PrimitiveKind] value.
 *
 * @property value The value contained by the object.
 */
data class PrimitiveValue(
    val value: Any?
) : Value {
    override fun toString(): String = "Value(${value.toClarifyString()})"
}

/**
 * A value that represents a value of [SerialKind].
 *
 * If the value is [PrimitiveKind], it should be [PrimitiveValue].
 *
 * If the value is [StructureKind] or [PolymorphicKind], it should be [StructureValue].
 */
sealed interface Value {
    fun isNullValue(): Boolean = this is PrimitiveValue && value == null
}

/**
 * A container that contains a [Value].
 */
sealed interface ValueContainer {
    val record: Value
}

/**
 * A template for `toString()` without misunderstanding.
 *
 * `String` should be enclosed with double quotes.
 *
 * `Char` should be enclosed with single quotes.
 */
private fun Any?.toClarifyString() =
    when (this) {
        null -> "null"
        is String -> "\"$this\""
        is Char -> "'$this'"
        else -> this.toString()
    }
