package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forge.parserKit.transition.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.hasIntersection
import com.github.andrewkuryan.forge.extensions.minOfSize

fun <N : Any> NSA<N>.applyHopcroft(): NSA<N> {
    var finalSets = setOf(finalStates, states - finalStates)
    var currentSets = setOf(finalStates, states - finalStates)

    while (currentSets.isNotEmpty()) {
        val currentSet = currentSets.first()

        val (newFinalSets, newCurrentSets) = getInTransitions(currentSet)
            .groupBy { it.guard.getBehavior() }
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
        if (group.hasIntersection(finalStates)) {
            newNSA.addFinalState(newStates.getValue(group.first()))
        }
        newNSA.addTransitions(
            (getInTransitions(group) + getOutTransitions(group)).map {
                it.copy(source = newStates.getValue(it.source), target = newStates.getValue(it.target))
            }
        )
    }

    return newNSA
}

private data class Behaviour<N : Any>(
    val input: InputSlice,
    val stack: StackSlice,
    val inputPreview: InputSlice,
    val stackSlice: StackSlice,
    val combinedStackPush: List<StackSignal.Preview>,
    val semanticAction: SemanticAction<N, *>?,
)

private fun <N : Any> Guard.Meaningful<N>.getBehavior(): Behaviour<N> =
    when (this) {
        is Guard.Input -> Behaviour(
            input, emptyList(),
            inputPreview, stackPreview,
            stackPushBefore + stackPushAfter,
            null
        )

        is Guard.Stack -> Behaviour(
            emptyList(), stack,
            inputPreview, stackPreview,
            stackPushBefore + rollupTarget + stackPushAfter,
            semanticAction
        )
    }