package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode

sealed interface StackSignal {
    sealed interface Frame : StackSignal
    sealed interface Preview : StackSignal

    sealed interface Read : Frame {
        data class Node<N : SyntaxNode>(val name: String, val value: N?) : Read {
            override fun toString() = "($name, $value)"
        }
    }

    sealed interface Push : Frame, Preview

    data object Bottom : Frame, Preview {
        override fun toString() = "$"
    }

    data class Symbol(val value: Char) : Read, Push, Preview {
        override fun toString() = value.toString()
    }

    data class NodeView(val name: String) : Preview {
        override fun toString() = name
    }

    data class Marker(val name: String) : Push, Preview {
        override fun toString() = "⁅$name⁆"
    }
}

value class StackSlice(val value: List<StackSignal.Preview>) {

    companion object {
        val EMPTY = StackSlice(emptyList())
    }

    val size: Int get() = value.size
    val isEmpty: Boolean get() = value.isEmpty()

    operator fun plus(other: StackSlice) = StackSlice(this.value + other.value)

    override fun toString() = if (isEmpty) "ε" else value.joinToString("")
}

value class StackPush(val value: List<StackSignal.Push>) {

    companion object {
        val EMPTY = StackPush(emptyList())
    }

    val isEmpty: Boolean get() = value.isEmpty()

    override fun toString() = if (isEmpty) "ε" else value.joinToString("")
}