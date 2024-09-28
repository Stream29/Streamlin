package io.github.stream29.streamlin.serialize.transform

import kotlin.Any

/**
 * A property that represents a key-value pair, or, a property of a object.
 *
 * @property key The key of the property.
 * @property value The value of the property.
 */
sealed interface Property {
    val key: PrimitiveValue
    val value: Value

    companion object {
        /**
         * Creates a property with the given key and value.
         *
         * @param key The key of the property. Only keys of [] are allowed.
         * @param value The value of the property. Values of [PrimitiveValue] or [StructureValue] are allowed.
         */
        operator fun invoke(key: Any?, value: Value) =
            when (value) {
                is PrimitiveValue -> PrimitiveProperty.of(key, value)
                is StructureValue -> StructureProperty.of(key, value)
            }
    }
    fun isNullProperty(): Boolean = value.isNullValue()
}

data class PrimitiveProperty(
    override val key: PrimitiveValue,
    override val value: PrimitiveValue
) : Property {
    companion object {
        fun of(key: Any?, value: Any?) = PrimitiveProperty(PrimitiveValue(key), PrimitiveValue(value))
    }

    override fun toString() = "Property(${key.value.toClarifyString()}=${value.value.toClarifyString()})"
}

data class StructureProperty(
    override val key: PrimitiveValue,
    override val value: StructureValue
) : Property {
    companion object {
        fun of(key: Any?, value: StructureValue) = StructureProperty(PrimitiveValue(key), value)
    }
    override fun toString() = "Property(key=${key.value.toClarifyString()}, value=$value)"
}

@Suppress("MemberVisibilityCanBePrivate")
data class StructureValue(
    val component: MutableList<Property> = mutableListOf()
) : Value, MutableList<Property> by component {
    override fun toString() = "Structure(component=$component)"
}

data class PrimitiveValue(
    val value: Any?
) : Value {
    override fun toString(): String = "Value(${value.toClarifyString()})"
}

sealed interface Value{
    fun isNullValue(): Boolean = this is PrimitiveValue && value == null
}

sealed interface ValueContainer {
    val record: Value
}

private fun Any?.toClarifyString() =
    when (this) {
        null -> "null"
        is String -> "\"$this\""
        is Char -> "'$this'"
        else -> this.toString()
    }
