package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.BNF.SyntaxNode

data class State(val index: Int) {
    override fun toString() = "S${index}"
}

abstract class AbstractNSA<N : SyntaxNode, T : Transition<N>> {
    private var internalInitState = State(0)
    val initState: State get() = internalInitState

    private val internalFinalStates = mutableSetOf<State>()
    val finalStates: Set<State> get() = internalFinalStates

    private val internalTransitionTable = mutableMapOf<State, MutableSet<T>>()
    val transitionTable: Map<State, Set<T>> get() = internalTransitionTable

    val states: Set<State> get() = transitionTable.keys + finalStates

    private var stateCount = 1
    private val inputSizes = mutableMapOf<Int, Int>()
    private val stackPreviewSizes = mutableMapOf<Int, Int>()

    val maxInputSize: Int get() = inputSizes.keys.maxOrNull() ?: 0
    val maxStackPreviewSize: Int = stackPreviewSizes.keys.maxOrNull() ?: 0

    fun nextState() = State(stateCount++)

    fun addTransition(transition: T): T {
        internalTransitionTable.getOrPut(transition.source) { mutableSetOf() }.add(transition)

        inputSizes[transition.inputSize] = (inputSizes[transition.inputSize] ?: 0) + 1
        stackPreviewSizes[transition.stackSize] = (stackPreviewSizes[transition.stackSize] ?: 0) + 1

        return transition
    }

    fun removeTransition(transition: T): T {
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
                .map { replaceVertexes(it, newState, if (it.target in states) newState else it.target) }
            val newInTransitions = getInTransitions(states)
                .map { replaceVertexes(it, if (it.source in states) newState else it.source, newState) }

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

    fun MeaningfulTransition<N>.replaceVertexes(source: State, target: State): MeaningfulTransition<N> {
        return when (this) {
            is InputTransition<N> -> copy(source = source, target = target)
            is StackTransition<N> -> copy(source = source, target = target)
        }
    }

    abstract fun replaceVertexes(transition: T, source: State, target: State): T
}

class ENSA<N : SyntaxNode> : AbstractNSA<N, Transition<N>>() {

    override fun replaceVertexes(transition: Transition<N>, source: State, target: State): Transition<N> {
        return when (transition) {
            is EmptyTransition<N> -> transition.copy(source = source, target = target)
            is MeaningfulTransition<N> -> transition.replaceVertexes(source, target)
        }
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

class NSA<N : SyntaxNode> : AbstractNSA<N, MeaningfulTransition<N>>() {

    override fun replaceVertexes(transition: MeaningfulTransition<N>, source: State, target: State) =
        transition.replaceVertexes(source, target)
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
                transitionTable[state]
                    ?.filterIsInstance<MeaningfulTransition<N>>()
                    ?.map { meaningfulClosure(it.target) to it } ?: listOf()
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