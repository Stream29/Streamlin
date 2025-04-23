# Streamlin

This is a kotlin common library that provides a set of utilities by Stream.

Modules are as follows.

You can find the examples in src/commonTest/kotlin/Examples.kt.

## Cache

This module provides a simple way to cache a function's output.

example:

```kotlin
// For cached function
val cacheMap = mutableMapOf<Int, Int>()
val cachedFunction = cacheWith(cacheMap) {
    println("Computing with param $it...")
    // Doing some heavy computation...
    it * 2
}
```

```kotlin
// Only compute once for 1
cachedFunction(1)
// Only compute once for 2
cachedFunction(2)
// No computation for 1 again
cachedFunction(1)
// No computation for 2 again
cachedFunction(2)
```

```kotlin
// For cached extension properties
class Example(val name: String)
val lazyMap = mutableMapOf<Example, Int>()
val Example.nameLength by lazy(lazyMap) { name.length }
val Example.nameOfFirstAccessedInstance by globalCached { name }
```

```kotlin
val example1 = Example("Stream")
val example2 = Example("Stream29")

println(example1.nameLength) // "Stream".length = 6
println(example2.nameLength) // "Stream29".length = 8

println(example1.nameOfFirstAccessedInstance) // "Stream"
println(example2.nameOfFirstAccessedInstance) // "Stream"
```

## PrettyPrint

This module provides a way to pretty print a data class's `toString()` result.

example:

```kotlin
data class PrettyPrint(
    val name: String = "Stream",
    val age: Int = 114514,
    val address: String = "Stream's home",
    val embedded: Embedded = Embedded(),
)
data class Embedded(
    val property1: String = "1",
    val property2: String = "2",
)
```
```kotlin
val example = PrettyPrint()
prettyPrintln(example)
/*
will print:
PrettyPrint(
  name=Stream,
  age=114514,
  address=Stream's home,
  embedded=Embedded(
    property1=1,
    property2=2
  )
)
 */
```

## AddToStdlib

This module provides a set of extension functions making chained calls easier.

### Filter

The `filter` function is an extension function version of `takeIf` in the Kotlin standard library.

Example:

```kotlin
class Example(val name: String)
```

```kotlin
val example = Example("Stream")
example.filter { name.startsWith("str", ignoreCase = true) } // example
example.filter { name.startsWith("str", ignoreCase = false) } // null
```

### Cast and SafeCast

The `cast` and `safeCast` functions provide convenient ways to cast objects.

Example:

```kotlin
val any: Any = "test"

// Cast will throw ClassCastException if the cast fails
val string: String = any.cast()

// SafeCast will return null if the cast fails
val safeString: String? = any.safeCast()
val safeInt: Int? = any.safeCast() // Returns null since any is a String
```

## AutoUpdateProperty

This module provides a way to create properties that automatically update their values when their dependencies change.

### Basic Usage

```kotlin
// Create a root property
val rootProperty = AutoUpdatePropertyRoot<Int>(sync = false, mode = AutoUpdateMode.PROPAGATE)

// Create subproperties that depend on the root property
val doubledProperty = rootProperty.subproperty { it * 2 }
val quadrupledProperty = doubledProperty.subproperty { it * 2 }

// Use property delegation for convenient access
var root by rootProperty
val doubled by doubledProperty
val quadrupled by quadrupledProperty

// Setting the root property will automatically update all dependent properties
root = 5
println(root)       // 5
println(doubled)    // 10
println(quadrupled) // 20

root = 10
println(root)       // 10
println(doubled)    // 20
println(quadrupled) // 40
```

### Proxied Properties

You can create a proxy for a property that works with a different type:

```kotlin
// Create a root property for Int
val intProperty = AutoUpdatePropertyRoot<Int>(initValue = 10)

// Create a proxy that works with String
val stringProperty = intProperty.proxied(
    rootToProxy = { it.toString() },
    proxyToRoot = { it.toInt() }
)

// Use property delegation
var intValue by intProperty
var stringValue by stringProperty

// The properties are synchronized
println(intValue)    // 10
println(stringValue) // "10"

intValue = 20
println(stringValue) // "20"

stringValue = "30"
println(intValue)    // 30
```

### Update Modes

AutoUpdateProperty supports two update modes:

- `AutoUpdateMode.PROPAGATE`: Updates are propagated to all dependent properties immediately when the root property changes.
- `AutoUpdateMode.LAZY`: Values are updated only when accessed, which can be more efficient when not all properties are used.

## DelegatingSerializer

This module provides a way to serialize and deserialize objects by delegating to another serializer.

Example:

```kotlin
// Original data class
@Serializable(with = PointSerializer::class)
data class Point(val x: Int, val y: Int)

// Delegate data class
@Serializable
data class PointDto(val coords: String) {
    companion object {
        fun fromPoint(point: Point): PointDto = PointDto("${point.x},${point.y}")
        fun toPoint(dto: PointDto): Point {
            val (x, y) = dto.coords.split(",").map { it.toInt() }
            return Point(x, y)
        }
    }
}

// Serializer using the DelegatingSerializer
class PointSerializer : KSerializer<Point> by DelegatingSerializer(
    fromDelegate = PointDto.Companion::toPoint,
    toDelegate = PointDto.Companion::fromPoint
)

// Usage
val point = Point(10, 20)
val json = Json.encodeToString(point) // {"coords":"10,20"}
val decoded = Json.decodeFromString<Point>(json) // Point(10, 20)
```
