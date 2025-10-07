package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forgeKit.transition.EmptyTransition
import com.github.andrewkuryan.forgeKit.transition.MeaningfulTransition
import com.github.andrewkuryan.forgeKit.transition.State
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.hasIntersection

private fun <N : Any> ENSA<N, *>.eClosure(current: Set<State>, visited: Set<State> = current): Set<State> =
    current
        .flatMap { state -> getOutTransitions(state).filterIsInstance<EmptyTransition<*>>().map { it.target } }
        .filter { it !in visited }
        .takeIf { it.isNotEmpty() }
        ?.toSet()
        ?.let { eClosure(it, visited + it) } ?: visited

private fun <N : Any> ENSA<N, *>.meaningfulClosure(start: State): Set<State> =
    eClosure(setOf(start))
        .filter { state -> state in finalStates || getOutTransitions(state).any { it !is EmptyTransition } }
        .toSet()

fun <N : Any> ENSA<N, *>.removeEmptyTransitions(): NSA<N> {
    val newNSA = NSA<N>()
    val initialClosure = meaningfulClosure(initState)

    val queue = mutableListOf(initialClosure to newNSA.initState)
    val processed = mutableMapOf(initialClosure to newNSA.initState)

    while (queue.isNotEmpty()) {
        val (oldStates, newState) = queue.removeAt(0)
        val newProcessed = oldStates
            .flatMap { state ->
                getOutTransitions(state)
                    .filterIsInstance<MeaningfulTransition<N>>()
                    .map { meaningfulClosure(it.target) to it }
            }
            .groupBy { it.first }
            .mapValues { (targetClosure, transitions) ->
                processed.getOrElse(targetClosure) { newNSA.nextState() }.apply {
                    newNSA.addTransitions(transitions.map { it.second.copy(source = newState, target = this) })
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

    if (initialClosure.hasIntersection(finalStates)) {
        newNSA.addFinalState(newNSA.initState)
    }

    return newNSA
}