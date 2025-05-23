package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.BNF.SemanticAction
import com.github.andrewkuryan.BNF.SyntaxNode
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.minOfSize

private data class Behaviour<N : SyntaxNode>(
    val input: InputSlice,
    val stack: StackSlice,
    val inputPreview: InputSlice,
    val stackSlice: StackSlice,
    val stackPush: StackPush,
    val semanticAction: SemanticAction<N>?,
)

private fun <N : SyntaxNode> MeaningfulTransition<N>.getBehavior(): Behaviour<N> =
    when (this) {
        is InputTransition -> Behaviour(
            input, StackSlice.EMPTY,
            inputPreview, stackPreview,
            StackPush(stackPushBefore.value + stackPushAfter.value),
            null
        )

        is StackTransition -> Behaviour(
            InputSlice.EMPTY, stack,
            inputPreview, stackPreview,
            StackPush(stackPushBefore.value + rollupTarget + stackPushAfter.value),
            semanticAction
        )
    }

fun <N : SyntaxNode> NSA<N>.applyHopcroft(): NSA<N> {
    var finalSets = setOf(finalStates, states - finalStates)
    var currentSets = setOf(finalStates, states - finalStates)

    while (currentSets.isNotEmpty()) {
        val currentSet = currentSets.first()

        val (newFinalSets, newCurrentSets) = getInTransitions(currentSet)
            .groupBy { it.getBehavior() }
            .values
            .map { transitions -> transitions.map { it.source }.toSet() }
            .fold(finalSets to currentSets.minusElement(currentSet)) { result, sourceSet ->
                result.first
                    .map { Triple(it, it.intersect(sourceSet), it - sourceSet) }
                    .filter { it.second.isNotEmpty() && it.third.isNotEmpty() }
                    .fold(result) { (actualFinalSets, actualCurrentSets), (finalSet, intersect, subtract) ->
                        val currentToAdd =
                            if (finalSet in actualCurrentSets) setOf(intersect, subtract)
                            else setOf(minOfSize(intersect, subtract))
                        Pair(
                            actualFinalSets.minusElement(finalSet) + setOf(intersect, subtract),
                            actualCurrentSets.minusElement(finalSet) + currentToAdd
                        )
                    }
            }

        finalSets = newFinalSets
        currentSets = newCurrentSets
    }

    val newNSA = NSA<N>()
    val newStates = finalSets
        .flatMap { group -> newNSA.nextState().let { newState -> group.map { it to newState } } }
        .toMap()

    for (group in finalSets) {
        if (initState in group) {
            newNSA.setInitState(newStates.getValue(group.first()))
        }
        if (group.intersect(finalStates).isNotEmpty()) {
            newNSA.addFinalState(newStates.getValue(group.first()))
        }
        for (transition in getInTransitions(group) + getOutTransitions(group)) {
            newNSA.addTransition(
                transition.replaceVertexes(newStates.getValue(transition.source), newStates.getValue(transition.target))
            )
        }
    }

    return newNSA
}