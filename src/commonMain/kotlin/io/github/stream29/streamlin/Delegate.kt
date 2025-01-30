package io.github.stream29.streamlin


public data object UninitializedValue

public enum class AutoUpdateMode {
    LAZY,
    PROPAGATE
}

public expect fun <T> AutoUpdatePropertyRoot(
    sync: Boolean = true,
    mode: AutoUpdateMode = AutoUpdateMode.PROPAGATE,
): AutoUpdatePropertyRoot<T>

public expect fun <T> AutoUpdatePropertyRoot(
    sync: Boolean = true,
    mode: AutoUpdateMode = AutoUpdateMode.PROPAGATE,
    initValue: T,
): AutoUpdatePropertyRoot<T>

public interface AutoUpdateProperty<T> {
    public fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R>
    public operator fun getValue(thisRef: Any?, property: Any?): T
}

public interface AutoUpdatePropertyRoot<T> : AutoUpdateProperty<T> {
    public operator fun setValue(thisRef: Any?, property: Any?, value: T)
}

internal class UnsafeLazyAutoUpdatePropertyRoot<T>(
    private var value: Any? = UninitializedValue,
) : AutoUpdatePropertyRoot<T> {
    internal var version: Int = 0
        private set

    override operator fun setValue(thisRef: Any?, property: Any?, value: T): Unit {
        this.value = value
        version++
    }

    override operator fun getValue(thisRef: Any?, property: Any?): T {
        if (value === UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R> =
        UnsafeLazyAutoUpdatePropertyNode(this, transform)
}

internal class UnsafeLazyAutoUpdatePropertyNode<T, V>(
    private val root: UnsafeLazyAutoUpdatePropertyRoot<T>,
    private val transform: (T) -> V,
) : AutoUpdateProperty<V> {
    private var version: Int = -1
    private var value: V? = null

    override operator fun getValue(thisRef: Any?, property: Any?): V {
        if (root.version != version) {
            value = transform(root.getValue(null, null))
            version = root.version
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    override fun <R> subproperty(transform: (V) -> R): AutoUpdateProperty<R> =
        UnsafeLazyAutoUpdatePropertyNode(root) { transform(this.transform(it)) }
}

internal class UnsafePropagateAutoUpdatePropertyRoot<T>(
    private var value: Any? = UninitializedValue
) : AutoUpdatePropertyRoot<T> {
    private val subpropertyList = mutableListOf<UnsafePropagateAutoUpdatePropertyNode<T, *>>()

    override operator fun setValue(thisRef: Any?, property: Any?, value: T): Unit {
        this.value = value
        subpropertyList.forEach { it.value = it.transform(value) }
    }

    override operator fun getValue(thisRef: Any?, property: Any?): T {
        if (value == UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun <R> subproperty(transform: (T) -> R): AutoUpdateProperty<R> =
        UnsafePropagateAutoUpdatePropertyNode(this, transform).also { subpropertyList.add(it) }
}

internal class UnsafePropagateAutoUpdatePropertyNode<T, V>(
    private val root: UnsafePropagateAutoUpdatePropertyRoot<T>,
    internal val transform: (T) -> V,
) : AutoUpdateProperty<V> {
    internal var value: Any? = UninitializedValue

    override operator fun getValue(thisRef: Any?, property: Any?): V {
        if (value === UninitializedValue) {
            throw IllegalStateException("Property not initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }

    override fun <R> subproperty(transform: (V) -> R): AutoUpdateProperty<R> =
        root.subproperty { transform(this.transform(it)) }
}