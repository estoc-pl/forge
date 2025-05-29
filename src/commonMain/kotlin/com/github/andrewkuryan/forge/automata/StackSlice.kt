package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode

sealed interface StackSignal {
    sealed interface Frame : StackSignal
    sealed interface Preview : StackSignal

    sealed interface Read : Frame {
        data class Node<N : SyntaxNode>(val name: String, val value: N?) : Read
    }

    sealed interface Push : Frame, Preview

    data object Bottom : Frame, Preview
    data class Symbol(val value: Char) : Read, Push, Preview
    data class NodeView(val name: String) : Preview
    data class Marker(val name: String) : Push, Preview
}

value class StackSlice(val value: List<StackSignal.Preview>) {

    companion object {
        val EMPTY = StackSlice(emptyList())
    }

    val size: Int get() = value.size
    val isEmpty: Boolean get() = value.isEmpty()

    operator fun plus(other: StackSlice) = StackSlice(this.value + other.value)
}

value class StackPush(val value: List<StackSignal.Push>) {

    companion object {
        val EMPTY = StackPush(emptyList())
    }

    val isEmpty: Boolean get() = value.isEmpty()
}