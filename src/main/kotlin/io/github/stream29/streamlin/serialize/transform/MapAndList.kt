package io.github.stream29.streamlin.serialize.transform

fun decodeAny(value: Value): Any? =
    when (value) {
        is NullValue -> null
        is PrimitiveValue -> value.value
        is StructureValue -> decodeStructure(value)
    }

fun decodeStructure(value: StructureValue): Any =
    when {
        value.component.isEmpty() -> emptyMap<String, Any>()
        value.component
            .foldIndexed(true) { index, acc, property ->
                acc && index == property.key.toIntOrNull()
            } ->
            decodeList(value)

        else -> decodeMap(value)
    }

fun decodeList(value: StructureValue): List<Any?> =
    value.component.map { decodeAny(it.value) }

fun decodeMap(value: StructureValue): Map<String, Any?> =
    value.component.associate { it.key to decodeAny(it.value) }

fun encodeAny(value: Any?): Value =
    when (value) {
        null -> NullValue
        is String -> PrimitiveValue(value)
        is Int -> PrimitiveValue(value)
        is Long -> PrimitiveValue(value)
        is Float -> PrimitiveValue(value)
        is Double -> PrimitiveValue(value)
        is Boolean -> PrimitiveValue(value)
        is Map<*, *> -> encodeMap(value)
        is List<*> -> encodeList(value)
        else -> encodeReflex(value)
    }

fun encodeReflex(value: Any) =
    value::class.java.declaredFields
        .asSequence()
        .filter { !it.isSynthetic }
        .map { it.isAccessible = true; it }
        .map { Property(it.name, encodeAny(it.get(value))) }
        .toMutableList()
        .let { StructureValue(it) }

fun encodeList(list: List<*>) =
    StructureValue().apply {
        list.forEachIndexed { index, element ->
            add(encodeAny(element).named(index.toString()))
        }
    }

fun encodeMap(map: Map<*, *>) =
    StructureValue().apply {
        map.forEach { (key, value) ->
            add(encodeAny(value).named(key.toString()))
        }
    }
