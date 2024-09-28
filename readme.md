# Streamlin

This is a kotlin common library that provides a set of utilities by Stream.

Modules are as follow.

## Cache

This module provides a simple way to cache a function's output.

example:

```kotlin
// You can cache the result of a function call with a mutable map.
val cacheMap = mutableMapOf<Int, Int>()
val cachedFunction = cacheWith(cacheMap) { it * 2 }

class Example(val name: String)
val lazyMap = mutableMapOf<Example, Int>()
// You can provide a mutable map to cache the result of a lazy property that performs like a member lazy property..
val Example.lazyNameLength by lazy(lazyMap) { name.length }
// You can make a global cached property for every instance of a class.
val Example.globalCached by globalCached { this::class.simpleName!! }
```

## PrettyPrint

This module provides a way to pretty print a data class's `toString()` result.

example:

```kotlin
data class Example(
    val name: String = "Stream",
    val age: Int = 114514,
    val address: String = "Stream's home",
    val embedded: Embedded = Embedded(),
)

data class Embedded(
    val property1: String = "1",
    val property2: String = "2",
)

val example = Example()
prettyPrintln(example)
```

## Functional

This module provides a set of functional utilities.

As for now, only `filter` is provided. It is a extension function version of `takeIf` in kotlin standard library.

## Serialization

This module provides a set of utilities for serialization.

### FromFunction

This module provides a way to deserialize a `serializable` object from a function.

You can use it to read something from a config or a JWT token.

example:

```kotlin
@Serializable
data class Example(
    val name: String = "Stream",
    val age: Int = 114514,
    val address: String = "Stream's home",
    val embedded: Embedded
)

@Serializable
data class Embedded(
    val property1: String = "1",
    val property2: String = "2",
)

val configMap = mapOf(
    "name" to "Stream",
    "age" to "114514",
    "embedded.property1" to "1",
    "embedded.property2" to "2",
)
val example = fromFunction<Example>(configMap::get)
```

### Transform

This module provides a way to transform a `serializable` object, a map or a list.

You can easily transform among them

example:

```kotlin
@Serializable
data class TestDefault(
    val name: String = "Stream",
    val age: Int = 12
)

@Serializable
data class TestTransform(
    val name: String = "Stream"
)

val value = Transformer.encodeToValue(TestDefault())
val transformed = Transformer.decodeFromValue<TestTransform>(value)
```

As long as the structure is compatible, you can transform smoothly.

If that instance is not a `serializable` object, it will be encoded by reflection.