package com.github.andrewkuryan.forge.automata

sealed class StackPushSignal : StackSignal()

sealed class StackSignal {
    data object Bottom : StackSignal() {
        override fun toString() = "$"
    }

    data class Symbol(val value: Char) : StackPushSignal() {
        override fun toString() = value.toString()
    }

    data class Node(val name: String) : StackPushSignal() {
        override fun toString() = name
    }

    data class Marker(val name: String) : StackPushSignal() {
        override fun toString() = "⁅$name⁆"
    }
}

value class StackSlice(val value: List<StackSignal>) {

    companion object {
        val EMPTY = StackSlice(emptyList())
    }

    val size: Int get() = value.size
    val isEmpty: Boolean get() = value.isEmpty()

    operator fun plus(other: StackSlice) = StackSlice(this.value + other.value)

    override fun toString() = if (isEmpty) "ε" else value.joinToString("")
}

value class StackPush(val value: List<StackPushSignal>) {

    companion object {
        val EMPTY = StackPush(emptyList())
    }

    val isEmpty: Boolean get() = value.isEmpty()

    override fun toString() = if (isEmpty) "ε" else value.joinToString("")
}