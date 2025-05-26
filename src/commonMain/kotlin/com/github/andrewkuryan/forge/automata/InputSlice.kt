package com.github.andrewkuryan.forge.automata

sealed class InputSignal {
    sealed class Unitary : InputSignal()

    data object EOI : Unitary() {
        override fun toString() = "┴"
    }

    data class Symbol(val value: Char) : Unitary() {
        override fun toString() = value.toString()
    }

    data class Range(val value: CharRange) : Unitary() {
        override fun toString() = "${value.first}-${value.last}"
    }

    data class Not(val first: Unitary, val rest: List<Unitary> = listOf()) : InputSignal() {
        override fun toString() = (listOf(first) + rest).joinToString(
            "",
            if (rest.isEmpty()) "^" else "[^",
            if (rest.isEmpty()) "" else "]"
        )
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