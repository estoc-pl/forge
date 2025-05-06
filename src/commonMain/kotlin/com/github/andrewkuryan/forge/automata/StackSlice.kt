package com.github.andrewkuryan.forge.automata

sealed interface StackSignal {
    data object Bottom : StackSignal {
        override fun toString() = "$"
    }

    data class Node(val name: String) : StackSignal {
        override fun toString() = name
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