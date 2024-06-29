package com.github.andrewkuryan.forge.automata

sealed interface InputSignal

sealed class Signal {
    object Empty : Signal() {
        override fun toString() = "ε"
    }

    object EOI : Signal(), InputSignal {
        override fun toString() = "┴"
    }
}

data class Letter(val value: Char) : Signal(), InputSignal {
    override fun toString() = value.toString()
}

data class Input(val signal: Signal, val preview: List<InputSignal> = listOf()) {
    companion object {
        val EMPTY = Input(Signal.Empty)
    }

    val isEmpty: Boolean get() = this == EMPTY

    override fun toString() = signal.toString() +
            if (preview.isNotEmpty()) preview.joinToString(", ", " (", ")") else ""
}