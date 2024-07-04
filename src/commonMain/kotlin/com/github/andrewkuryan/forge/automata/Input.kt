package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.extensions.startsWith

sealed interface InputSignal

sealed class Signal {
    object Empty : Signal() {
        override fun toString() = "ε"
    }

    object EOI : Signal(), InputSignal {
        override fun toString() = "┴"
    }

    data class Letter(val value: Char) : Signal(), InputSignal {
        override fun toString() = value.toString()
    }
}

data class Input(val signal: Signal, val preview: List<InputSignal> = listOf()) {
    companion object {
        val EMPTY = Input(Signal.Empty)
    }

    val isEmpty: Boolean get() = this == EMPTY

    val size: Int = preview.size + if (signal is Signal.Empty) 0 else 1

    val signals: List<InputSignal> =
        when (signal) {
            is Signal.Empty -> preview
            is InputSignal -> listOf(signal) + preview
        }

    fun startsWith(other: Input) = signals.startsWith(other.signals)

    override fun toString() = signal.toString() +
            if (preview.isNotEmpty()) preview.joinToString(", ", "(", ")") else ""
}