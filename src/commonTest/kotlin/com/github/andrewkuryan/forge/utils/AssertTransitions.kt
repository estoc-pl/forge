package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.State
import com.github.andrewkuryan.forge.automata.defaultFormat
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class StateRef(var value: State? = null) {

    override fun toString() = "StateRef(value=$value)"
}

fun NSA<*>.assertTransitions(
    initRef: StateRef,
    finalRef: StateRef,
    transitions: Map<StateRef, List<Pair<TransitionBody, StateRef>>>,
) {
    initRef.value = initState

    assertEquals(1, finalStates.size)
    finalRef.value = finalStates.first()

    for ((ref, stateTransitions) in transitions) {
        ref.value?.let {
            assertNotNull(it, "There were no transitions to $stateTransitions yet")
            assertTransitions(it, stateTransitions)
        }
    }
    assertEquals(transitions.size, transitions.map { it.key.value }.toSet().size, "State refs have duplicates")
}

fun NSA<*>.assertTransitions(source: State, transitions: List<Pair<TransitionBody, StateRef>>) {
    if (transitions.isEmpty()) {
        assertNull(transitionTable[source], "State $source has outgoing transitions")
    } else {
        assertNotNull(transitionTable[source], "State $source does not have transitions")
        assertEquals(
            transitions.size,
            transitionTable.getValue(source).size,
            "The number of transitions from $source does not match"
        )
    }

    for ((transition, target) in transitions) {
        val foundTransitions = transitionTable.getValue(source).filter {
            it.input == transition.input && it.stackPreview == transition.stackPreview && it.action == transition.action
        }
        assertEquals(1, foundTransitions.size, "Cannot find an unambiguous transition $transition in $source")

        if (target.value != null) {
            assertEquals(
                target.value,
                foundTransitions.first().target,
                "Transition ${foundTransitions.first().defaultFormat()} was expected to lead to ${target.value}"
            )
        } else {
            target.value = foundTransitions.first().target
        }
    }
}