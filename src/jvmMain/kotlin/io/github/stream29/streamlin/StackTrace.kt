@file:JvmName("StackTraceKt")

package io.github.stream29.streamlin

/**
 * Provides programmatic access to the stack trace information of the execution point.
 * Returns an [List] of stack trace elements,
 * each representing one stack frame.  The zeroth element of the list
 * (assuming the list's length is non-zero) represents the top of the
 * stack, which is the last method invocation in the sequence.  Typically,
 * this is the point at which this property is evaluated.
 * The last element of the array (assuming the array's length is non-zero)
 * represents the bottom of the stack, which is the first method invocation
 * in the sequence.
 *
 * Some virtual machines may, under some circumstances, omit one
 * or more stack frames from the stack trace.  In the extreme case,
 * a virtual machine that has no stack trace information concerning
 * this property is permitted to be a zero-length list from its evaluation environment.
 *
 * @return an list of stack trace elements representing the execution point where the property is evaluated.
 */
public val contextStackTrace: List<StackTraceElement> get() = getStackTraceFiltered()

/**
 * Returns the name of the source file where the property is evaluated.
 * Generally, this corresponds
 * to the file name attribute of the relevant
 * file (as per *The Java Virtual Machine Specification, Section
 *  4.7.7*).  In some systems, the name may refer to some source code unit
 * other than a file, such as an entry in a source repository.
 *
 * @return the name of the file containing the execution point
 *         where the property is evaluated, or `null` if
 *         this information is unavailable.
 */
public val contextFileName: String? get() = currentStackTraceElement.fileName

/**
 * @return the name of the JVM class where this property is evaluated
 */
public val contextClassName: String get() = currentStackTraceElement.className

/**
 * Returns the name of the method where the property is evaluated.  If the execution point is
 * contained in an instance or class initializer, this property will return
 * the appropriate *special method name*, [java.lang.constant.ConstantDescs.INIT_NAME]
 * or [java.lang.constant.ConstantDescs.CLASS_INIT_NAME], as per *Section 3.9*
 * of *The Java Virtual Machine Specification*.
 *
 * @return the name of the method where the property is evaluated.
 */
public val contextMethodName: String get() = currentStackTraceElement.methodName

/**
 * Returns the line number of the source line where the property is evaluated.
 * Generally, this is
 * derived from the `LineNumberTable` attribute of the relevant
 * class file (as per *The Java Virtual Machine
 * Specification, Section 4.7.8*).
 *
 * @return the line number of the source line where the property is evaluated, or a negative
 *         number if this information is unavailable.
 */
public val contextLineNumber: Int get() = currentStackTraceElement.lineNumber

internal val currentStackTraceElement: StackTraceElement get() = contextStackTrace.firstOrNull()
    ?: throw IllegalStateException("Stack trace unavailable. (Getting a empty stacktrace on your JVM)")

/**
 * To get the stack trace of the current thread.
 */
internal class StackTraceException : Exception()

/**
 * Get the stack trace of the current thread, excluding functions in this file.
 */
internal fun getStackTraceFiltered(): List<StackTraceElement> =
    StackTraceException().stackTrace.filterNot { it.className == "io.github.stream29.streamlin.StackTraceKt" }