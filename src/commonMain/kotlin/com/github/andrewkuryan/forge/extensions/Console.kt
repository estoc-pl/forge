package com.github.andrewkuryan.forge.extensions

enum class ConsoleColor(val number: Int) {
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36)
}

fun success(message: String) = printLog("SUCCESS", ConsoleColor.GREEN, message)
fun warning(message: String) = printLog("WARNING", ConsoleColor.YELLOW, message)
fun error(message: String) = printLog("ERROR", ConsoleColor.RED, message)

fun printLog(tag: String, color: ConsoleColor, message: String) {
    println(colorWrap("[$tag]: ", color) + message.split("\n").joinToString("\n") { colorWrap(it, color) })
}

fun colorWrap(str: String, color: ConsoleColor) = "\u001B[${color.number}m$str\u001B[0m"