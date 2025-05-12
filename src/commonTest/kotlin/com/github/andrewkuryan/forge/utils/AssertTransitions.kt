package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.State
import com.github.andrewkuryan.forge.automata.defaultFormat
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StateRef(var value: State? = null) {

    override fun toString() = "StateRef(value=$value)"
}

fun NSA<*>.assertTransitions(
    initRef: StateRef,
    finalRef: StateRef,
    transitions: Map<StateRef, List<Pair<TransitionBody, StateRef>>>,
) = assertTransitions(initRef, listOf(finalRef), transitions)

fun NSA<*>.assertTransitions(
    initRef: StateRef,
    finalRefs: List<StateRef>,
    transitions: Map<StateRef, List<Pair<TransitionBody, StateRef>>>,
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
        assertNotNull(originalFinalState, "Cannot find final state ${finalRef.value}")
        originalFinalStates.remove(originalFinalState)
    }
}

fun NSA<*>.assertTransitions(source: State, transitions: List<Pair<TransitionBody, StateRef>>) {
    assertEquals(transitions.size, getOutTransitions(source).size, "Number of transitions from $source does not match")

    val originalTransitions = getOutTransitions(source).toMutableSet()
    for ((transition, target) in transitions) {
        val foundTransitions = originalTransitions.filter { transition.isSameAs(it) }
        assertEquals(1, foundTransitions.size, "Cannot find an unambiguous transition [$transition] in $source")

        val originalTransition = foundTransitions.first()
        if (target.value != null) {
            assertEquals(
                target.value,
                originalTransition.target,
                "Transition ${originalTransition.defaultFormat()} was expected to lead to ${target.value}"
            )
        } else {
            target.value = foundTransitions.first().target
        }
        originalTransitions.remove(originalTransition)
    }
}