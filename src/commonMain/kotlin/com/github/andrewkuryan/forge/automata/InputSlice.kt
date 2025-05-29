package com.github.andrewkuryan.forge.automata

sealed class InputSignal {
    sealed class Unitary : InputSignal()

    data object EOI : Unitary()
    data class Symbol(val value: Char) : Unitary()
    data class Range(val value: CharRange) : Unitary()
    data class Not(val first: Unitary, val rest: List<Unitary> = listOf()) : InputSignal()
}

value class InputSlice(val value: List<InputSignal>) {

    companion object {
        val EMPTY = InputSlice(listOf())
    }

    val size: Int get() = value.size
    val isEmpty: Boolean get() = value.isEmpty()

    operator fun plus(other: InputSlice) = InputSlice(this.value + other.value)
}