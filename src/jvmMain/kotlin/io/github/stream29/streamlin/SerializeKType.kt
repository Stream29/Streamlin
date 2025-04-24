package io.github.stream29.streamlin

import kotlin.jvm.internal.ClassReference
import kotlin.jvm.internal.TypeReference
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance

internal fun KType.serializable(): SKType =
    this.cast<TypeReference>().run {
        STypeReference(
            jClass = classifier.cast<ClassReference>().jClass.serializable(),
            arguments = arguments.map { it.serializable() },
            flags = getField("flags") as Int,
        )
    }

internal fun Class<*>.serializable(): SJClass =
    when {
        isArray -> SArrayClass(componentType.serializable())
        isPrimitive -> SPrimitiveClass(name)
        else -> SPlainClass(name)
    }

internal fun STypeProjection.deserialize() =
    KTypeProjection(
        variance?.deserialized(),
        kType.deserialize()
    )

internal fun KTypeProjection.serializable() =
    STypeProjection(
        kType = type?.serializable()!!,
        variance = variance?.serializable(),
    )

internal fun KVariance.serializable() = when (this) {
    KVariance.INVARIANT -> SKVariance.INVARIANT
    KVariance.IN -> SKVariance.IN
    KVariance.OUT -> SKVariance.OUT
}

internal fun SKVariance.deserialized() = when (this) {
    SKVariance.INVARIANT -> KVariance.INVARIANT
    SKVariance.IN -> KVariance.IN
    SKVariance.OUT -> KVariance.OUT
}

internal inline fun <reified T> T.getField(name: String): Any? {
    val field = T::class.java.getDeclaredField(name)
    field.isAccessible = true
    return field.get(this)
}