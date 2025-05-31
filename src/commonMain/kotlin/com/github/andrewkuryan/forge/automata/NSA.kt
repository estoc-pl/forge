package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forgeKit.State
import com.github.andrewkuryan.forgeKit.Transition
import com.github.andrewkuryan.forgeKit.SyntaxNode
import com.github.andrewkuryan.forgeKit.MeaningfulTransition
import com.github.andrewkuryan.forge.extensions.hasIntersection

typealias TransitionTable<T> = MutableMap<State, MutableSet<T>>

open class ENSA<N : SyntaxNode, T : Transition<N>> {
    private var internalInitState = State(0)
    val initState: State get() = internalInitState

    private val internalFinalStates = mutableSetOf<State>()
    val finalStates: Set<State> get() = internalFinalStates

    private val transitionTable: TransitionTable<T> = mutableMapOf()
    private val reversedTransitionTable: TransitionTable<T> = mutableMapOf()

    val states: Set<State> get() = transitionTable.keys + finalStates

    private var stateCount = 1
    private val inputSizes = mutableMapOf<Int, Int>()
    private val stackPreviewSizes = mutableMapOf<Int, Int>()

    val maxInputSize: Int get() = inputSizes.keys.maxOrNull() ?: 0
    val maxStackPreviewSize: Int get() = stackPreviewSizes.keys.maxOrNull() ?: 0

    fun nextState() = State(stateCount++)

    fun setInitState(state: State) = state.apply { internalInitState = this }
    fun addFinalState(state: State) = state.apply { internalFinalStates.add(state) }

    fun <NT : T> addTransition(transition: NT): NT {
        transitionTable.getOrPut(transition.source) { mutableSetOf() }.add(transition)
        reversedTransitionTable.getOrPut(transition.target) { mutableSetOf() }.add(transition)

        inputSizes[transition.guard.inputSize] = (inputSizes[transition.guard.inputSize] ?: 0) + 1
        stackPreviewSizes[transition.guard.stackSize] = (stackPreviewSizes[transition.guard.stackSize] ?: 0) + 1

        return transition
    }

    fun addTransitions(transitions: List<T>) = transitions.onEach { addTransition(it) }

    private fun <NT : T> removeTransition(transition: NT): NT {
        if (transitionTable[transition.source] != null) {
            inputSizes[transition.guard.inputSize] = inputSizes.getValue(transition.guard.inputSize) - 1
            stackPreviewSizes[transition.guard.stackSize] = stackPreviewSizes.getValue(transition.guard.stackSize) - 1
        }

        removeTableTransition(transitionTable, transition.source, transition)
        removeTableTransition(reversedTransitionTable, transition.target, transition)

        return transition
    }

    private fun removeTransitions(transitions: List<T>) = transitions.onEach { removeTransition(it) }

    fun getInTransitions(state: State) = getInTransitions(setOf(state))
    fun getInTransitions(states: Set<State>) =
        states.fold(listOf<T>()) { acc, state -> acc + (reversedTransitionTable[state] ?: listOf()) }

    fun getOutTransitions(state: State) = getOutTransitions(setOf(state))
    fun getOutTransitions(states: Set<State>) =
        states.fold(listOf<T>()) { acc, state -> acc + (transitionTable[state] ?: listOf()) }

    fun removeStates(states: Set<State>) {
        removeTransitions(getInTransitions(states) + getOutTransitions(states))

        internalFinalStates.removeAll(finalStates.intersect(states))
    }

    private fun removeTableTransition(table: TransitionTable<T>, state: State, transition: T) {
        if (table[state]?.size == 1) {
            table.remove(state)
        } else {
            table[state]?.remove(transition)
        }
    }
}

class NSA<N : SyntaxNode> : ENSA<N, MeaningfulTransition<N>>() {

    @Throws(MultipleInitStatesException::class)
    fun createMergedState(states: Set<State>): State =
        if (states.size > 1) {
            val newState = nextState()
            if (initState in states) {
                throw MultipleInitStatesException()
            }
            if (states.hasIntersection(finalStates)) {
                addFinalState(newState)
            }
            val newOutTransitions = getOutTransitions(states)
                .map { it.copy(source = newState, target = if (it.target in states) newState else it.target) }
            val newInTransitions = getInTransitions(states)
                .map { it.copy(source = if (it.source in states) newState else it.source, target = newState) }

            addTransitions(newOutTransitions + newInTransitions)

            newState
        } else states.first()
}