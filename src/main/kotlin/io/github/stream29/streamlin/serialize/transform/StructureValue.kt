package io.github.stream29.streamlin.serialize.transform

import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.Any

sealed interface Property {
    val key: String
    val value: Value
}

@ExperimentalSerializationApi
class PrimitiveProperty(
    override val key: String,
    value: Any
) : Property {
    override val value = PrimitiveValue(value)
    override fun toString(): String {
        return "Property(${key.toClarifyString()}=${value.value.toClarifyString()})"
    }
}

@ExperimentalSerializationApi
class NullProperty(
    override val key: String
) : Property {
    override val value = NullValue
    override fun toString(): String {
        return "Property(${key.toClarifyString()}=null)"
    }
}

@ExperimentalSerializationApi
class StructureProperty(
    override val key: String,
    override val value: StructureValue
) : Property {
    override fun toString(): String {
        return "Property(key=${key.toClarifyString()}, value=$value)"
    }
}

@ExperimentalSerializationApi
@Suppress("MemberVisibilityCanBePrivate")
class StructureValue(
    val component: MutableList<Property> = mutableListOf()
) : Value, MutableList<Property> by component {
    override fun toString(): String {
        return "Structure(component=$component)"
    }
}

object NullValue : Value {
    override fun toString(): String {
        return "NullValue()"
    }
}

@ExperimentalSerializationApi
class PrimitiveValue(
    val value: Any
) : Value {
    override fun toString(): String =
        "Value(${value.toClarifyString()})"
}

sealed interface Value

private fun Any?.toClarifyString() =
    when (this) {
        null -> "null"
        is String -> "\"$this\""
        is Char -> "'$this'"
        else -> this.toString()
    }

@ExperimentalSerializationApi
sealed interface ValueContainer {
    val record: Value
}