package io.github.stream29.streamlin.serialize

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
        return "Property($key=$value)"
    }
}

@ExperimentalSerializationApi
class StructureProperty(
    override val key: String,
    val value: StructureValue
) : Property {
    override fun toString(): String {
        return "Property(key=$key, value=$value)"
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

@ExperimentalSerializationApi
@JvmInline
value class PrimitiveValue(
    val value: Any
) : Value {
    override fun toString(): String {
        return "Value($value)"
    }
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