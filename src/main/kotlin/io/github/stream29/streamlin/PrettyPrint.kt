package io.github.stream29.streamlin

fun prettyPrintln(any: Any?) {
    prettyPrint(any)
    println()
}

fun prettyPrint(any: Any?) = prettyPrint(any.toString())
fun prettyPrint(text: String) = print(text.toPrettyFormat())

private val leftBracket = setOf('{', '[', '(')
private val rightBracket = setOf('}', ']', ')')
private val blank = setOf(' ', '\t', '\n')

fun String.toPrettyFormat(): String {
    val indent = " "
    val buffer = StringBuilder()
    var lastChar: Char? = null
    var eatSpace = 0
    var level = 0
    this.forEach {
        when (it) {
            in leftBracket -> {
                buffer.append("$it\n")
                level += 2
                buffer.append(indent.repeat(level))
            }

            in rightBracket -> {
                level -= 2
                if(lastChar in leftBracket) {
                    while(buffer.last() in blank){
                        buffer.deleteCharAt(buffer.length - 1)
                    }
                }else{
                    buffer.append("\n" + indent.repeat(level))
                }
                buffer.append(it)
            }

            ',' -> {
                buffer.append("$it\n")
                buffer.append(indent.repeat(level))
                eatSpace++
            }

            ' ' -> {
                if (eatSpace == 0) {
                    buffer.append(it)
                    eatSpace--
                }
            }

            else -> buffer.append(it)
        }
        lastChar = it
    }
    return buffer.toString()
}