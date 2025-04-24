@file:JvmName("StackTraceKt")

package io.github.stream29.streamlin

public val contextStackTrace: List<StackTraceElement> get() = getStackTraceFiltered()
public val contextFileName: String? get() = currentStackTraceElement.fileName
public val contextClassName: String get() = currentStackTraceElement.className
public val contextMethodName: String get() = currentStackTraceElement.methodName
public val contextLineNumber: Int? get() = currentStackTraceElement.lineNumber

internal val currentStackTraceElement: StackTraceElement get() = contextStackTrace.first()

/**
 * To get the stack trace of the current thread.
 */
internal class StackTraceException : Exception()

/**
 * Get the stack trace of the current thread, excluding functions in this file.
 */
internal fun getStackTraceFiltered(): List<StackTraceElement> =
    StackTraceException().stackTrace.filterNot { it.className == "io.github.stream29.streamlin.StackTraceKt" }