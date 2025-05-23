package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.commonPrefix
import com.github.andrewkuryan.forge.extensions.commonSuffix
import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode
import com.github.andrewkuryan.forge.extensions.hasIntersection

private val STUB_STATE = State(-1)

fun <N : SyntaxNode> NSA<N>.leftFactorize(): NSA<N> {
    val newNSA = NSA<N>()

    val queue = mutableListOf(setOf(initState) to newNSA.initState)
    val processed = mutableMapOf(setOf(initState) to newNSA.initState)

    while (queue.isNotEmpty()) {
        val (oldStates, newState) = queue.removeAt(0)
        val newProcessed = getOutTransitions(oldStates)
            .fold(setOf<Pair<MeaningfulTransition<N>, Set<State>>>()) { acc, transition ->
                acc.find { it.first.canHCombine(transition) }
                    ?.let { entry ->
                        val (combinable, states) = entry
                        acc - entry + (combinable.hCombine(transition, STUB_STATE) to (states + transition.target))
                    }
                    ?: (acc + (transition to setOf(transition.target)))
            }
            .groupBy { it.second }
            .mapValues { (states, transitions) ->
                processed.getOrElse(states) { newNSA.nextState() }.apply {
                    newNSA.addTransitions(transitions.map { it.first.replaceVertexes(newState, this) })
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

private fun <N : SyntaxNode> MeaningfulTransition<N>.hCombine(
    other: MeaningfulTransition<N>,
    commonTarget: State,
): MeaningfulTransition<N> =
    when {
        this is InputTransition && other is InputTransition -> hCombine(other, commonTarget)
        this is StackTransition && other is StackTransition -> hCombine(other, commonTarget)
        else -> throw Exception("Cannot combine transitions of different types")
    }

private fun <N : SyntaxNode> InputTransition<N>.hCombine(other: InputTransition<N>, commonTarget: State) =
    copy(
        inputPreview = inputPreview.hCombine(other.inputPreview),
        stackPreview = stackPreview.hCombine(other.stackPreview),
        target = commonTarget
    )

private fun <N : SyntaxNode> StackTransition<N>.hCombine(other: StackTransition<N>, commonTarget: State) =
    copy(
        inputPreview = inputPreview.hCombine(other.inputPreview),
        stackPreview = stackPreview.hCombine(other.stackPreview),
        target = commonTarget
    )

private fun InputSlice.hCombine(other: InputSlice) = InputSlice(commonPrefix(value, other.value))
private fun StackSlice.hCombine(other: StackSlice) = StackSlice(commonSuffix(value, other.value))

private fun MeaningfulTransition<*>.canHCombine(other: MeaningfulTransition<*>) =
    when {
        this is InputTransition && other is InputTransition -> canHCombine(other)
        this is StackTransition && other is StackTransition -> canHCombine(other)
        else -> false
    } && inputPreview.canHCombine(other.inputPreview) &&
            stackPreview.canHCombine(other.stackPreview) &&
            stackPushBefore == other.stackPushBefore &&
            stackPushAfter == other.stackPushAfter &&
            !isLoop && !other.isLoop

private fun InputTransition<*>.canHCombine(other: InputTransition<*>) = input == other.input

private fun StackTransition<*>.canHCombine(other: StackTransition<*>) =
    stack == other.stack && rollupTarget == other.rollupTarget && semanticAction == other.semanticAction

private fun InputSlice.canHCombine(other: InputSlice) =
    isEmpty || other.isEmpty || commonPrefix(value, other.value).isNotEmpty()

private fun StackSlice.canHCombine(other: StackSlice) =
    isEmpty || other.isEmpty || commonSuffix(value, other.value).isNotEmpty()