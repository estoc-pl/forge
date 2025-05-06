package com.github.andrewkuryan.forge.automata

sealed interface InputSignal {
    object EOI : InputSignal {
        override fun toString() = "┴"
    }
}

value class InputSlice(val value: List<InputSignal>) {

    companion object {
        val EMPTY = InputSlice(listOf())
    }

    val size: Int get() = value.size
    val isEmpty: Boolean get() = value.isEmpty()

    operator fun plus(other: InputSlice) = InputSlice(this.value + other.value)

    override fun toString() = if (isEmpty) "ε" else value.joinToString("")
}