package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.BNF.SyntaxNode

data class State(val index: Int) {
    override fun toString() = "S${index}"
}

typealias TransitionTable<T> = MutableMap<State, MutableSet<T>>

abstract class AbstractNSA<N : SyntaxNode, T : Transition<N>> {
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
    val maxStackPreviewSize: Int = stackPreviewSizes.keys.maxOrNull() ?: 0

    fun nextState() = State(stateCount++)

    fun setInitState(state: State) {
        internalInitState = state
    }

    fun addFinalState(state: State) {
        internalFinalStates.add(state)
    }

    fun addTransition(transition: T): T {
        transitionTable.getOrPut(transition.source) { mutableSetOf() }.add(transition)
        reversedTransitionTable.getOrPut(transition.target) { mutableSetOf() }.add(transition)

        inputSizes[transition.inputSize] = (inputSizes[transition.inputSize] ?: 0) + 1
        stackPreviewSizes[transition.stackSize] = (stackPreviewSizes[transition.stackSize] ?: 0) + 1

        return transition
    }

    fun removeTransition(transition: T): T {
        if (transitionTable[transition.source] != null) {
            inputSizes[transition.inputSize] = inputSizes.getValue(transition.inputSize) - 1
            stackPreviewSizes[transition.stackSize] = stackPreviewSizes.getValue(transition.stackSize) - 1
        }

        removeTableTransition(transitionTable, transition.source, transition)
        removeTableTransition(reversedTransitionTable, transition.target, transition)

        return transition
    }

    fun getInTransitions(state: State) = getInTransitions(setOf(state))
    fun getInTransitions(states: Set<State>) =
        states.fold(listOf<T>()) { acc, state -> acc + (reversedTransitionTable[state] ?: listOf()) }

    fun getOutTransitions(state: State) = getOutTransitions(setOf(state))
    fun getOutTransitions(states: Set<State>) =
        states.fold(listOf<T>()) { acc, state -> acc + (transitionTable[state] ?: listOf()) }

    fun removeStates(states: Set<State>) {
        for (transition in getInTransitions(states) + getOutTransitions(states)) {
            removeTransition(transition)
        }
        internalFinalStates.removeAll(finalStates.intersect(states))
    }

    fun clearUnreachableStates() {
        val initReachable = getReachableFrom(setOf(initState))
        val finalReachable = getReachableTo(finalStates)

        removeStates(states - initReachable.intersect(finalReachable))
    }

    private fun getReachableFrom(states: Set<State>, visited: Set<State> = states): Set<State> =
        getOutTransitions(states).map { it.target }.filter { it !in visited }
            .takeIf { it.isNotEmpty() }
            ?.let { getReachableFrom(it.toSet(), visited + it) } ?: visited

    private fun getReachableTo(states: Set<State>, visited: Set<State> = states): Set<State> =
        getInTransitions(states).map { it.source }.filter { it !in visited }
            .takeIf { it.isNotEmpty() }
            ?.let { getReachableTo(it.toSet(), visited + it) } ?: visited

    private fun removeTableTransition(table: TransitionTable<T>, state: State, transition: T) {
        if (table[state]?.size == 1) {
            table.remove(state)
        } else {
            table[state]?.remove(transition)
        }
    }
}

class ENSA<N : SyntaxNode> : AbstractNSA<N, Transition<N>>() {

    private fun eClosure(current: Set<State>, visited: Set<State> = current): Set<State> =
        current
            .flatMap { state -> getOutTransitions(state).filterIsInstance<EmptyTransition<*>>().map { it.target } }
            .filter { it !in visited }
            .takeIf { it.isNotEmpty() }
            ?.toSet()
            ?.let { eClosure(it, visited + it) } ?: visited

    fun meaningfulClosure(start: State): Set<State> =
        eClosure(setOf(start))
            .filter { state -> state in finalStates || getOutTransitions(state).any { it !is EmptyTransition } }
            .toSet()
}

class NSA<N : SyntaxNode> : AbstractNSA<N, MeaningfulTransition<N>>() {

    @Throws(MultipleInitStatesException::class)
    fun createMergedState(states: Set<State>): State =
        if (states.size > 1) {
            val newState = nextState()
            if (initState in states) {
                throw MultipleInitStatesException()
            }
            if (states.intersect(finalStates).isNotEmpty()) {
                addFinalState(newState)
            }
            val newOutTransitions = getOutTransitions(states)
                .map { it.replaceVertexes(newState, if (it.target in states) newState else it.target) }
            val newInTransitions = getInTransitions(states)
                .map { it.replaceVertexes(if (it.source in states) newState else it.source, newState) }

            for (transition in newOutTransitions + newInTransitions) {
                addTransition(transition)
            }
            newState
        } else states.first()
}

fun <N : SyntaxNode> ENSA<N>.removeEmptyTransitions(): NSA<N> {
    val nsa = NSA<N>()
    val initialClosure = meaningfulClosure(initState)
    val newInitialState = nsa.nextState()

    val queue = mutableListOf(initialClosure to newInitialState)
    val processed = mutableMapOf(initialClosure to newInitialState)

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
                val newTarget = processed[targetClosure] ?: nsa.nextState()
                transitions.forEach {
                    nsa.addTransition(it.second.replaceVertexes(newState, newTarget))
                }
                newTarget
            }
            .filter { it.key !in processed }
            .onEach {
                if (it.key.intersect(finalStates).isNotEmpty()) {
                    nsa.addFinalState(it.value)
                }
            }
            .toList()

        processed.putAll(newProcessed)
        queue.addAll(newProcessed)
    }

    nsa.setInitState(newInitialState)
    if (initialClosure.intersect(finalStates).isNotEmpty()) {
        nsa.addFinalState(newInitialState)
    }

    return nsa
}