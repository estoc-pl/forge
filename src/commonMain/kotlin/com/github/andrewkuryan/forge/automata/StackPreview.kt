package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.extensions.endsWith

sealed class StackSignal {
    data object Bottom : StackSignal() {
        override fun toString() = "$"
    }

    data class Letter(val name: String) : StackSignal() {
        override fun toString() = name
    }
}

data class StackPreview(val signals: List<StackSignal>) {
    companion object {
        val ANY = StackPreview(listOf())
    }

    val isAny: Boolean get() = this == ANY

    operator fun plus(signal: StackSignal) = StackPreview(signals + signal)
    operator fun plus(other: StackPreview) = StackPreview(signals + other.signals)

    val size: Int = signals.size

    fun endsWith(other: StackPreview) = signals.endsWith(other.signals)

    override fun toString() = if (isAny) "*" else signals.joinToString("")
}