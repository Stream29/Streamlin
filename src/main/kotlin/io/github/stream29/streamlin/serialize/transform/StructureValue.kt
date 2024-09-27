package io.github.stream29.streamlin.serialize.transform

import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.Any

sealed interface Property {
    val key: String
}

@ExperimentalSerializationApi
class PrimitiveProperty(
    override val key: String,
    val value: Any
) : Property {
    override fun toString(): String {
        return "Property(${key.toClarifyString()}=${value.toClarifyString()})"
    }
}

@ExperimentalSerializationApi
class NullProperty(
    override val key: String
) : Property {
    override fun toString(): String {
        return "Property(${key.toClarifyString()}=null)"
    }
}

@ExperimentalSerializationApi
class StructureProperty(
    override val key: String,
    val value: StructureValue
) : Property {
    override fun toString(): String {
        return "Property(key=${key.toClarifyString()}, value=$value)"
    }
}

@ExperimentalSerializationApi
class StructureValue(
    val component: MutableList<Property> = mutableListOf()
) : Value {
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

@ExperimentalSerializationApi
class Record(
    val component: MutableList<Value> = mutableListOf()
) {
    override fun toString(): String {
        return "Record$component"
    }
}

sealed interface Value

private fun Any?.toClarifyString() =
    when (this) {
        null -> "null"
        is String -> "\"$this\""
        is Char -> "'$this'"
        else -> this.toString()
    }