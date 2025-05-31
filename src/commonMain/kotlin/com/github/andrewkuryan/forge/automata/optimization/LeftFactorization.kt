package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forgeKit.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.commonPrefix
import com.github.andrewkuryan.forge.extensions.commonSuffix
import com.github.andrewkuryan.forge.extensions.hasIntersection

fun <N : SyntaxNode> NSA<N>.leftFactorize(): NSA<N> {
    val newNSA = NSA<N>()

    val queue = mutableListOf(setOf(initState) to newNSA.initState)
    val processed = mutableMapOf(setOf(initState) to newNSA.initState)

    while (queue.isNotEmpty()) {
        val (oldStates, newState) = queue.removeAt(0)
        val newProcessed = getOutTransitions(oldStates)
            .fold(setOf<Triple<Guard.Meaningful<N>, Boolean, Set<State>>>()) { acc, transition ->
                acc.find { it.first.canHCombine(transition.guard) && !it.second && !transition.isLoop }
                    ?.let { entry ->
                        val (combinable, _, states) = entry
                        acc - entry + Triple(combinable.hCombine(transition.guard), false, states + transition.target)
                    }
                    ?: (acc + Triple(transition.guard, transition.isLoop, setOf(transition.target)))
            }
            .groupBy { it.third }
            .mapValues { (states, transitions) ->
                processed.getOrElse(states) { newNSA.nextState() }.apply {
                    newNSA.addTransitions(transitions.map { MeaningfulTransition(newState, this, it.first) })
                }
            }
            .filter { it.key !in processed }
            .onEach {
                if (it.key.hasIntersection(finalStates)) {
                    newNSA.addFinalState(it.value)
                }
            }
            .toList()

        processed.putAll(newProcessed)
        queue.addAll(newProcessed)
    }

    if (initState in finalStates) {
        newNSA.addFinalState(newNSA.initState)
    }

    return newNSA
}

private fun <N : SyntaxNode> Guard.Meaningful<N>.hCombine(other: Guard.Meaningful<N>): Guard.Meaningful<N> =
    when {
        this is Guard.Input && other is Guard.Input -> hCombine(other)
        this is Guard.Stack && other is Guard.Stack -> hCombine(other)
        else -> throw Exception("Cannot combine transitions of different types")
    }

private fun <N : SyntaxNode> Guard.Input<N>.hCombine(other: Guard.Input<N>) =
    copy(
        inputPreview = inputPreview.hCombine(other.inputPreview),
        stackPreview = stackPreview.hCombine(other.stackPreview)
    )

private fun <N : SyntaxNode> Guard.Stack<N>.hCombine(other: Guard.Stack<N>) =
    copy(
        inputPreview = inputPreview.hCombine(other.inputPreview),
        stackPreview = stackPreview.hCombine(other.stackPreview)
    )

private fun InputSlice.hCombine(other: InputSlice) = commonPrefix(this, other)
private fun StackSlice.hCombine(other: StackSlice) = commonSuffix(this, other)

private fun Guard.Meaningful<*>.canHCombine(other: Guard.Meaningful<*>) =
    when {
        this is Guard.Input && other is Guard.Input -> canHCombine(other)
        this is Guard.Stack && other is Guard.Stack -> canHCombine(other)
        else -> false
    } && inputPreview.canHCombine(other.inputPreview) &&
            stackPreview.canHCombine(other.stackPreview) &&
            stackPushBefore == other.stackPushBefore &&
            stackPushAfter == other.stackPushAfter

private fun Guard.Input<*>.canHCombine(other: Guard.Input<*>) = input == other.input

private fun Guard.Stack<*>.canHCombine(other: Guard.Stack<*>) =
    stack == other.stack && rollupTarget == other.rollupTarget && semanticAction == other.semanticAction

private fun InputSlice.canHCombine(other: InputSlice) =
    isEmpty() || other.isEmpty() || commonPrefix(this, other).isNotEmpty()

private fun StackSlice.canHCombine(other: StackSlice) =
    isEmpty() || other.isEmpty() || commonSuffix(this, other).isNotEmpty()