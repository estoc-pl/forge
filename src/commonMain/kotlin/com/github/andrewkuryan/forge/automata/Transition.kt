package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode
import com.github.andrewkuryan.forge.extensions.grammar.SemanticAction

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

sealed class MeaningfulTransition<N : SyntaxNode> : Transition<N>() {
    abstract val inputPreview: InputSlice
    abstract val stackPreview: StackSlice
    abstract val stackPushBefore: StackPush
    abstract val stackPushAfter: StackPush
}

data class InputTransition<N : SyntaxNode>(
    val input: InputSlice,
    override val inputPreview: InputSlice,
    override val stackPreview: StackSlice,
    override val source: State,
    override val target: State,
    override val stackPushBefore: StackPush = StackPush.EMPTY,
    override val stackPushAfter: StackPush = StackPush.EMPTY,
) : MeaningfulTransition<N>() {

    override val inputSize = input.size + inputPreview.size
    override val stackSize = stackPreview.size
}

data class StackTransition<N : SyntaxNode>(
    val stack: StackSlice,
    val rollupTarget: StackSignal.NodeView,
    val semanticAction: SemanticAction<N>?,
    override val inputPreview: InputSlice,
    override val stackPreview: StackSlice,
    override val source: State,
    override val target: State,
    override val stackPushBefore: StackPush = StackPush.EMPTY,
    override val stackPushAfter: StackPush = StackPush.EMPTY,
) : MeaningfulTransition<N>() {

    override val inputSize = inputPreview.size
    override val stackSize = stack.size + stackPreview.size
}

fun <N : SyntaxNode> MeaningfulTransition<N>.replaceVertexes(source: State, target: State): MeaningfulTransition<N> {
    return when (this) {
        is InputTransition<N> -> copy(source = source, target = target)
        is StackTransition<N> -> copy(source = source, target = target)
    }
}