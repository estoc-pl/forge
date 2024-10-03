package com.github.andrewkuryan.forge.automata

sealed class StackSignal {
    data object Bottom : StackSignal() {
        override fun toString() = "$"
    }

    data class Letter(val name: String) : StackSignal() {
        override fun toString() = name
    }
}

value class StackSlice(val value: List<StackSignal>) {

    companion object {
        val EMPTY = StackSlice(emptyList())
    }

    val size: Int get() = value.size
    val isEmpty: Boolean get() = value.isEmpty()

    override fun toString() = if (isEmpty) "*" else value.joinToString("")
}