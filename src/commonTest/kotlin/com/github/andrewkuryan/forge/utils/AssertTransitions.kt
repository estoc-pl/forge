package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.automata.format.format
import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class StateRef(var value: State? = null) {

    override fun toString() = "StateRef(value=${value?.format()})"
}

fun NSA<*>.assertTransitions(
    initRef: StateRef,
    finalRef: StateRef,
    transitions: Map<StateRef, List<Pair<Guard.Meaningful<SyntaxNode>, StateRef>>>,
) = assertTransitions(initRef, listOf(finalRef), transitions)

fun NSA<*>.assertTransitions(
    initRef: StateRef,
    finalRefs: List<StateRef>,
    transitions: Map<StateRef, List<Pair<Guard.Meaningful<SyntaxNode>, StateRef>>>,
) {
    initRef.value = initState

    assertEquals(finalRefs.size, finalStates.size, "Number of final states does not match")

    for ((ref, stateTransitions) in transitions) {
        ref.value?.let {
            assertNotNull(it, "There were no transitions to $stateTransitions yet")
            assertTransitions(it, stateTransitions)
        }
    }
    assertEquals(transitions.size, transitions.map { it.key.value }.toSet().size, "State refs have duplicates")

    val originalFinalStates = finalStates.toMutableSet()
    for (finalRef in finalRefs) {
        val originalFinalState = originalFinalStates.find { finalRef.value == it }
        assertNotNull(originalFinalState, "Cannot find final state ${finalRef.value?.format()}")
        originalFinalStates.remove(originalFinalState)
    }
}

fun NSA<*>.assertTransitions(source: State, transitions: List<Pair<Guard.Meaningful<SyntaxNode>, StateRef>>) {
    assertEquals(
        transitions.size,
        getOutTransitions(source).size,
        "Number of transitions from ${source.format()} does not match"
    )

    val originalTransitions = getOutTransitions(source).toMutableSet()
    for ((guard, target) in transitions) {
        val foundTransitions = originalTransitions.filter { guard == it.guard }
        assertNotEquals(0, foundTransitions.size, "Cannot find transition [${guard.format()}] in $source")

        val originalTransition = foundTransitions.first()
        if (target.value != null) {
            assertEquals(
                target.value,
                originalTransition.target,
                "Transition ${originalTransition.format()} was expected to lead to ${target.value?.format()}"
            )
        } else {
            target.value = foundTransitions.first().target
        }
        originalTransitions.remove(originalTransition)
    }
}