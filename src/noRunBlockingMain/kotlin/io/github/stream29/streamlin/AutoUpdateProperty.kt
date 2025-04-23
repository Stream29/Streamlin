package io.github.stream29.streamlin

public actual fun <T> AutoUpdatePropertyRoot(
    sync: Boolean,
    mode: AutoUpdateMode
): AutoUpdatePropertyRoot<T> = when (mode) {
    AutoUpdateMode.LAZY -> UnsafeLazyAutoUpdatePropertyRoot()
    AutoUpdateMode.PROPAGATE -> UnsafePropagateAutoUpdatePropertyRoot()
}

public actual fun <T> AutoUpdatePropertyRoot(
    sync: Boolean,
    mode: AutoUpdateMode,
    initValue: T
): AutoUpdatePropertyRoot<T> = when (mode) {
    AutoUpdateMode.LAZY -> UnsafeLazyAutoUpdatePropertyRoot(initValue)
    AutoUpdateMode.PROPAGATE -> UnsafePropagateAutoUpdatePropertyRoot(initValue)
}