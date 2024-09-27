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
    override fun toString() = "Property(${key.toClarifyString()}=${value.value.toClarifyString()})"
}

@ExperimentalSerializationApi
class NullProperty(
    override val key: String
) : Property {
    override val value = NullValue
    override fun toString() = "Property(${key.toClarifyString()}=null)"
}

@ExperimentalSerializationApi
class StructureProperty(
    override val key: String,
    override val value: StructureValue
) : Property {
    override fun toString() = "Property(key=${key.toClarifyString()}, value=$value)"
}

@ExperimentalSerializationApi
@Suppress("MemberVisibilityCanBePrivate")
class StructureValue(
    val component: MutableList<Property> = mutableListOf()
) : Value, MutableList<Property> by component {
    override fun toString() = "Structure(component=$component)"
    override fun named(name: String) = StructureProperty(name, this)
}

@ExperimentalSerializationApi
object NullValue : Value {
    override fun toString() = "NullValue()"
    override fun named(name: String) = NullProperty(name)
}

@ExperimentalSerializationApi
class PrimitiveValue(
    val value: Any
) : Value {
    override fun toString(): String = "Value(${value.toClarifyString()})"
    override fun named(name: String) = PrimitiveProperty(name, value)
}

sealed interface Value {
    fun named(name: String): Property
}

@ExperimentalSerializationApi
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
