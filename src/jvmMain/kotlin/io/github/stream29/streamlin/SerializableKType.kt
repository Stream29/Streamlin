package io.github.stream29.streamlin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.lang.reflect.Array
import kotlin.jvm.internal.ClassReference
import kotlin.jvm.internal.TypeReference
import kotlin.reflect.KType

@Serializable
internal sealed interface SKType {
    fun deserialize(): KType
}

@Serializable
internal data class STypeReference(
    val jClass: SJClass,
    val arguments: List<STypeProjection>,
    val flags: Int,
) : SKType {
    override fun deserialize(): TypeReference =
        TypeReference(
            classifier = ClassReference(jClass = jClass.deserialized()),
            arguments = arguments.map { it.deserialize() },
            platformTypeUpperBound = null,
            flags = flags
        )
}

@Serializable
internal data class STypeProjection(
    val kType: SKType,
    val variance: SKVariance?,
)

@Serializable
internal sealed interface SJClass {
    fun deserialized(): Class<*>
}

@Serializable
@SerialName("plain")
internal data class SPlainClass(
    val name: String,
) : SJClass {
    override fun deserialized(): Class<*> = Class.forName(name)
}

@Serializable
@SerialName("array")
internal data class SArrayClass(
    val elementClass: SJClass,
) : SJClass {
    override fun deserialized(): Class<*> = Array.newInstance(elementClass.deserialized(), 0).javaClass
}

@Serializable
@SerialName("primitive")
internal data class SPrimitiveClass(
    val name: String,
) : SJClass {
    override fun deserialized(): Class<*> = when (name) {
        "boolean" -> Boolean::class.javaPrimitiveType!!
        "byte" -> Byte::class.javaPrimitiveType!!
        "short" -> Short::class.javaPrimitiveType!!
        "int" -> Int::class.javaPrimitiveType!!
        "long" -> Long::class.javaPrimitiveType!!
        "float" -> Float::class.javaPrimitiveType!!
        "double" -> Double::class.javaPrimitiveType!!
        "char" -> Char::class.javaPrimitiveType!!
        else -> error("Unknown primitive type: $name")
    }
}

@Serializable
internal enum class SKVariance {
    INVARIANT,
    IN,
    OUT,
}