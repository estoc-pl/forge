package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.translation.SyntaxNode

data class TransitionBody(
    val input: Input,
    val stackPreview: StackPreview,
    val action: StackAction<SyntaxNode>? = null,
) {

    override fun toString() = "$input, $stackPreview / ${action?.toString() ?: "-"}"
}

fun read(input: Char, stackPreview: String) =
    TransitionBody(
        Input(Signal.Letter(input)),
        StackPreview(parseStackPreview(stackPreview)),
        Push(StackSignal.Letter(input.toString()))
    )

fun rollup(stackPreview: String, top: String, target: String) =
    TransitionBody(
        Input.EMPTY,
        StackPreview(parseStackPreview(stackPreview)),
        Rollup(StackSignal.Letter(target), parseStackPreview(top), null)
    )

fun exit(stackPreview: String) = TransitionBody(Input(Signal.EOI), StackPreview(parseStackPreview(stackPreview)))

private fun parseStackPreview(stackPreview: String) =
    stackPreview.map {
        when (it) {
            '$' -> StackSignal.Bottom
            else -> StackSignal.Letter(it.toString())
        }
    }