package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.BNF.SyntaxNode
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.commonPrefix
import com.github.andrewkuryan.forge.extensions.commonSuffix

private fun InputTransition<*>.canHCombine(other: InputTransition<*>) =
    input == other.input && stackPush == other.stackPush

private fun StackTransition<*>.canHCombine(other: StackTransition<*>) =
    stack == other.stack && stackPush == other.stackPush && semanticAction == other.semanticAction

private fun InputSlice.canHCombine(other: InputSlice) =
    isEmpty || other.isEmpty || commonPrefix(value, other.value).isNotEmpty()

private fun StackSlice.canHCombine(other: StackSlice) =
    isEmpty || other.isEmpty || commonSuffix(value, other.value).isNotEmpty()

private fun Transition<*>.canHCombine(other: Transition<*>) =
    when {
        this is InputTransition && other is InputTransition -> canHCombine(other)
        this is StackTransition && other is StackTransition -> canHCombine(other)
        else -> false
    } && !isLoop && !other.isLoop &&
            inputPreview.canHCombine(other.inputPreview) &&
            stackPreview.canHCombine(other.stackPreview)

private fun InputSlice.hCombine(other: InputSlice) = InputSlice(commonPrefix(value, other.value))
private fun StackSlice.hCombine(other: StackSlice) = StackSlice(commonSuffix(value, other.value))

private fun <N : SyntaxNode> InputTransition<N>.hCombine(other: InputTransition<N>, commonTarget: State) =
    InputTransition<N>(
        input,
        stackPush,
        source,
        commonTarget,
        inputPreview.hCombine(other.inputPreview),
        stackPreview.hCombine(other.stackPreview),
    )

private fun <N : SyntaxNode> StackTransition<N>.hCombine(other: StackTransition<N>, commonTarget: State) =
    StackTransition(
        stack,
        stackPush,
        semanticAction,
        source,
        commonTarget,
        inputPreview.hCombine(other.inputPreview),
        stackPreview.hCombine(other.stackPreview),
    )

private fun <N : SyntaxNode> NSA<N>.hCombine(transition1: Transition<N>, transition2: Transition<N>): Transition<N> {
    removeTransition(transition1)
    removeTransition(transition2)
    val newTarget = mergeStates(setOf(transition1.target, transition2.target))
    val newTransition = when {
        transition1 is InputTransition && transition2 is InputTransition -> transition1.hCombine(transition2, newTarget)
        transition1 is StackTransition && transition2 is StackTransition -> transition1.hCombine(transition2, newTarget)
        else -> throw Exception("Cannot combine transitions of different types")
    }
    addTransition(newTransition)
    return newTransition
}

fun <N : SyntaxNode> NSA<N>.hCombine(transitions: List<Transition<N>>): List<Transition<N>> =
    transitions.fold(listOf()) { acc, transition ->
        val combinable = acc.find { it.canHCombine(transition) }
        if (combinable != null) acc - combinable + hCombine(combinable, transition)
        else acc + transition
    }