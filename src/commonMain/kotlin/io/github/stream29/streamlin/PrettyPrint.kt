package io.github.stream29.streamlin

/**
 * Pretty print a string presentation of an object and end the line.
 *
 * Increase indentation for nested structures.
 *
 * @param any The object to pretty print.
 */
public fun prettyPrintln(any: Any?) {
    prettyPrint(any)
    println()
}

/**
 * Pretty print a string presentation of an object.
 *
 * Increase indentation for nested structures.
 *
 * @param any The object to pretty print.
 */
public fun prettyPrint(any: Any?): Unit = prettyPrint(any.toString())

/**
 * Pretty print a string and end the line.
 *
 * Increase indentation for nested structures.
 *
 * @param text The string to pretty print.
 */
public fun prettyPrint(text: String): Unit = print(text.toPrettyFormat())

private val leftBracket = setOf('{', '[', '(')
private val rightBracket = setOf('}', ']', ')')
private val blank = setOf(' ', '\t', '\n')

/**
 * Pretty format a string.
 *
 * Increase indentation for nested structures.
 *
 * @return The pretty formatted string.
 */
public fun String.toPrettyFormat(): String {
    val indent = " "
    val buffer = StringBuilder()
    var lastChar: Char? = null
    var eatSpace = -1
    var level = 0
    this.forEachIndexed { index, it ->
        when (it) {
            in leftBracket -> {
                buffer.append("$it\n")
                level += 2
                buffer.append(indent.repeat(level))
            }

            in rightBracket -> {
                level -= 2
                if (lastChar in leftBracket) {
                    while (buffer.last() in blank) {
                        buffer.deleteAt(buffer.length - 1)
                    }
                } else {
                    buffer.append("\n" + indent.repeat(level))
                }
                buffer.append(it)
            }

            ',' -> {
                buffer.append("$it\n")
                buffer.append(indent.repeat(level))
                eatSpace = index + 1
            }

            ' ' -> if (eatSpace != index) buffer.append(it)

            else -> buffer.append(it)
        }
        lastChar = it
    }
    return buffer.toString()
}