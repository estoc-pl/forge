package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.BNF.SemanticAction
import com.github.andrewkuryan.BNF.SyntaxNode

sealed class Transition<N : SyntaxNode> {
    abstract val source: State
    abstract val target: State

    abstract val inputSize: Int
    abstract val stackSize: Int

    val isLoop: Boolean get() = source == target
}

data class EmptyTransition<N : SyntaxNode>(
    override val source: State,
    override val target: State,
) : Transition<N>() {
    override val inputSize = 0
    override val stackSize = 0
}

data class InputTransition<N : SyntaxNode>(
    val input: InputSlice,
    val stackPush: StackSlice,
    val inputPreview: InputSlice,
    val stackPreview: StackSlice,
    override val source: State,
    override val target: State,
) : Transition<N>() {

    override val inputSize = input.size + inputPreview.size
    override val stackSize = stackPreview.size
}

data class StackTransition<N : SyntaxNode>(
    val stack: StackSlice,
    val stackPush: StackSignal,
    val semanticAction: SemanticAction<N>?,
    val inputPreview: InputSlice,
    val stackPreview: StackSlice,
    override val source: State,
    override val target: State,
) : Transition<N>() {

    override val inputSize = inputPreview.size
    override val stackSize = stack.size + stackPreview.size
}