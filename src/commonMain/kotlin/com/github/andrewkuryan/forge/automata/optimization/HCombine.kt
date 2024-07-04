package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forge.automata.Input
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.StackPreview
import com.github.andrewkuryan.forge.automata.Transition
import com.github.andrewkuryan.forge.extensions.commonPrefix
import com.github.andrewkuryan.forge.extensions.commonSuffix
import com.github.andrewkuryan.forge.translation.SyntaxNode

private fun Transition<*>.canHCombine(other: Transition<*>) =
    !isLoop && !other.isLoop && action == other.action && input.signal == other.input.signal &&
            (stackPreview.isAny || other.stackPreview.isAny ||
                    commonSuffix(stackPreview.signals, other.stackPreview.signals).isNotEmpty())

private fun Input.hCombine(other: Input) = Input(signal, commonPrefix(preview, other.preview))
private fun StackPreview.hCombine(other: StackPreview) = StackPreview(commonSuffix(signals, other.signals))

private fun <N : SyntaxNode> NSA<N>.hCombine(transition1: Transition<N>, transition2: Transition<N>): Transition<N> {
    removeTransition(transition1)
    removeTransition(transition2)
    val newTarget = mergeStates(setOf(transition1.target, transition2.target))
    val newTransition = Transition(
        transition1.source,
        newTarget,
        transition1.input.hCombine(transition2.input),
        transition1.stackPreview.hCombine(transition2.stackPreview),
        transition1.action,
    )
    addTransition(newTransition)
    return newTransition
}

fun <N : SyntaxNode> NSA<N>.hCombine(transitions: List<Transition<N>>): List<Transition<N>> =
    transitions.fold(listOf()) { acc, transition ->
        val combinable = acc.find { it.canHCombine(transition) }
        if (combinable != null) acc - combinable + hCombine(combinable, transition)
        else acc + transition
    }