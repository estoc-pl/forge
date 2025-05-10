package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.BNF.SyntaxNode

data class State(val index: Int) {
    override fun toString() = "S${index}"
}

fun <N : SyntaxNode> Transition<N>.replaceVertexes(source: State, target: State): Transition<N> {
    return when (this) {
        is InputTransition<N> -> copy(source = source, target = target)
        is StackTransition<N> -> copy(source = source, target = target)
        is EmptyTransition<N> -> copy(source = source, target = target)
    }
}

class NSA<N : SyntaxNode> {
    private var internalInitState = State(0)
    val initState: State get() = internalInitState

    private val internalFinalStates = mutableSetOf<State>()
    val finalStates: Set<State> get() = internalFinalStates

    private val internalTransitionTable = mutableMapOf<State, MutableSet<Transition<N>>>()
    val transitionTable: Map<State, Set<Transition<N>>> get() = internalTransitionTable

    val states: Set<State> get() = transitionTable.keys + finalStates

    private var stateCount = 1
    private val inputSizes = mutableMapOf<Int, Int>()
    private val stackPreviewSizes = mutableMapOf<Int, Int>()

    val maxInputSize: Int get() = inputSizes.keys.maxOrNull() ?: 0
    val maxStackPreviewSize: Int = stackPreviewSizes.keys.maxOrNull() ?: 0

    fun nextState() = State(stateCount++)

    fun addTransition(transition: Transition<N>): Transition<N> {
        internalTransitionTable.getOrPut(transition.source) { mutableSetOf() }.add(transition)

        inputSizes[transition.inputSize] = (inputSizes[transition.inputSize] ?: 0) + 1
        stackPreviewSizes[transition.stackSize] = (stackPreviewSizes[transition.stackSize] ?: 0) + 1

        return transition
    }

    fun removeTransition(transition: Transition<N>): Transition<N> {
        val sourceTransitions = internalTransitionTable[transition.source]
        if (sourceTransitions?.size == 1) {
            internalTransitionTable.remove(transition.source)
        } else {
            sourceTransitions?.remove(transition)
        }

        if (sourceTransitions != null) {
            inputSizes[transition.inputSize] = inputSizes.getValue(transition.inputSize) - 1
            stackPreviewSizes[transition.stackSize] = stackPreviewSizes.getValue(transition.stackSize) - 1
        }

        return transition
    }

    fun setInitState(state: State) {
        internalInitState = state
    }

    @Throws(UnreachableFinalStateException::class)
    fun addFinalState(state: State) {
        if (isUnreachable(state)) throw UnreachableFinalStateException(state)
        else internalFinalStates.add(state)
    }

    private fun getInTransitions(states: Set<State>) =
        internalTransitionTable
            .mapValues { (_, value) -> value.filter { it.target in states } }
            .filter { it.value.isNotEmpty() }
            .values.flatten()

    private fun getOutTransitions(states: Set<State>) =
        transitionTable.filter { (key, _) -> key in states }.values.flatten()

    fun removeStates(states: Set<State>) {
        for (state in states) {
            internalTransitionTable.remove(state)
        }
        for (transition in getInTransitions(states)) {
            removeTransition(transition)
            if (transitionTable.getValue(transition.source).isEmpty()) {
                internalTransitionTable.remove(transition.source)
            }
        }
    }

    @Throws(MultipleInitStatesException::class)
    fun createMergedState(states: Set<State>): State =
        if (states.size > 1) {
            val newState = nextState()
            if (initState in states) {
                throw MultipleInitStatesException()
            }
            if (states.intersect(finalStates).isNotEmpty()) {
                internalFinalStates.add(newState)
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

    private fun hasInTransitions(state: State) =
        transitionTable.values.flatten().any { it.target == state }

    private fun isUnreachable(state: State) = !hasInTransitions(state) && state != initState

    fun clearUnreachableStates() {
        val reachable = mutableSetOf(initState)
        val queue = mutableListOf(initState)
        while (queue.isNotEmpty()) {
            transitionTable[queue.removeAt(0)]
                ?.map { it.target }
                ?.filter { it !in reachable }
                ?.let {
                    queue.addAll(it)
                    reachable.addAll(it)
                }
        }
        val unreachable = transitionTable.keys - reachable
        removeStates(unreachable)
        internalFinalStates.removeAll(unreachable)
    }

    private fun eClosure(current: Set<State>, visited: Set<State> = current): Set<State> {
        return current
            .flatMap { state ->
                transitionTable[state]?.filterIsInstance<EmptyTransition<*>>()?.map { it.target } ?: emptyList()
            }
            .filter { it !in visited }
            .takeIf { it.isNotEmpty() }
            ?.toSet()
            ?.let { eClosure(it, visited + it) } ?: visited
    }

    fun meaningfulClosure(start: State) =
        eClosure(setOf(start))
            .filter { state ->
                state in finalStates || !transitionTable[state]?.filter { it !is EmptyTransition }.isNullOrEmpty()
            }
            .toSet()
}

fun <N : SyntaxNode> NSA<N>.removeEmptyTransitions(): NSA<N> {
    val nsa = NSA<N>()
    val initialClosure = meaningfulClosure(initState)
    val newInitialState = nsa.nextState()

    val queue = mutableListOf(newInitialState to initialClosure)
    val processed = mutableMapOf(initialClosure to newInitialState)

    while (queue.isNotEmpty()) {
        val (newState, oldStates) = queue.removeAt(0)
        val nextStates = oldStates.flatMap { state ->
            transitionTable[state]?.filter { it !is EmptyTransition }?.map { transition ->
                val targetClosure = meaningfulClosure(transition.target)
                val newTarget = processed[targetClosure] ?: nsa.nextState()
                val newTransition = nsa.addTransition(transition.replaceVertexes(newState, newTarget))
                newTransition to targetClosure
            } ?: listOf()
        }
            .filter { it.second !in processed }
            .groupBy { it.second }
            .values
            .map { transitions ->
                val combinedTarget = if (transitions.size > 1) nsa.nextState() else transitions.first().first.target
                val targetClosure = transitions.first().second
                transitions.onEach { (transition) ->
                    nsa.removeTransition(transition)
                    nsa.addTransition(transition.replaceVertexes(newState, combinedTarget))
                }
                combinedTarget to targetClosure
            }
            .onEach {
                processed[it.second] = it.first
                if (it.second.intersect(finalStates).isNotEmpty()) {
                    nsa.addFinalState(it.first)
                }
            }

        queue.addAll(nextStates)
    }

    nsa.setInitState(newInitialState)

    return nsa
}