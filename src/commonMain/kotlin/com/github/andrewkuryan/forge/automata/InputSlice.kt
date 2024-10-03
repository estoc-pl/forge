package com.github.andrewkuryan.forge.automata

sealed class InputSignal {
    object EOI : InputSignal() {
        override fun toString() = "┴"
    }

    data class Letter(val value: Char) : InputSignal() {
        override fun toString() = value.toString()
    }
}

value class InputSlice(val value: List<InputSignal>) {

    companion object {
        val EMPTY = InputSlice(listOf())
    }

    val size: Int get() = value.size
    val isEmpty: Boolean get() = value.isEmpty()

    override fun toString() = if (isEmpty) "ε" else value.joinToString("")
}