package com.github.andrewkuryan.forge.automata

sealed interface BaseInputSignal

sealed class InputSignal {
    data object EOI : InputSignal(), BaseInputSignal {
        override fun toString() = "┴"
    }

    data class Symbol(val value: Char) : InputSignal(), BaseInputSignal {
        override fun toString() = value.toString()
    }

    data class Range(val value: CharRange) : InputSignal(), BaseInputSignal {
        override fun toString() = "${value.first}-${value.last}"
    }

    data class Not(val first: BaseInputSignal, val rest: List<BaseInputSignal> = listOf()) : InputSignal() {
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