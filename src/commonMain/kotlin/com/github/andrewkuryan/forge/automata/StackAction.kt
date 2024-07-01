package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.translation.SemanticAction
import com.github.andrewkuryan.forge.translation.SyntaxNode

sealed class StackAction<N : SyntaxNode>

data class Push<N : SyntaxNode>(val letter: StackSignal.Letter) : StackAction<N>() {
    override fun toString() = letter.toString()
}

data class Rollup<N : SyntaxNode>(
    val target: StackSignal.Letter,
    val top: List<StackSignal>,
    val semanticAction: SemanticAction<N>?,
) : StackAction<N>() {
    override fun toString() = "${top.joinToString("")} → $target"
}