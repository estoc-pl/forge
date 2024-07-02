package com.github.andrewkuryan.forge.automata

import com.github.andrewkuryan.forge.translation.NodeBuilder
import com.github.andrewkuryan.forge.translation.SyntaxNode

data class State(val index: Int) {
    override fun toString() = "S${index}"
}

data class Transition<N : SyntaxNode>(
    val source: State,
    val target: State,
    val input: Input = Input.EMPTY,
    val stackPreview: StackPreview = StackPreview.ANY,
    val action: StackAction<N>? = null,
)

class NSA<N : SyntaxNode>(val nodeBuilder: NodeBuilder<N>) {
    inner class Port(val enter: State, val innerExit: State) {
        val exit: State = nextState()
        private val barriers = mutableMapOf<StackPreview, Transition<N>>()

        fun addBarrier(barrier: StackPreview) {
            if (barriers.none { barrier.endsWith(it.key) }) {
                val transition = addTransition(Transition(innerExit, exit, Input.EMPTY, barrier))
                barriers.filter { it.key.endsWith(barrier) }.forEach {
                    removeTransition(it.value)
                    barriers.remove(it.key)
                }
                barriers[barrier] = transition
            }
        }
    }

    private var internalInitState = State(0)
    val initState: State get() = internalInitState

    private val internalFinalStates = mutableSetOf<State>()
    val finalStates: Set<State> get() = internalFinalStates

    private val internalTransitionTable = mutableMapOf<State, MutableSet<Transition<N>>>()
    val transitionTable: Map<State, Set<Transition<N>>> get() = internalTransitionTable

    private var stateCount = 1
    private val inputSizes = mutableMapOf<Int, Int>()
    private val stackPreviewSizes = mutableMapOf<Int, Int>()

    val maxInputSize: Int get() = inputSizes.keys.maxOrNull() ?: 0
    val maxStackPreviewSize: Int = stackPreviewSizes.keys.maxOrNull() ?: 0

    fun nextState() = State(stateCount++)

    fun addTransition(transition: Transition<N>): Transition<N> {
        internalTransitionTable.getOrPut(transition.source) { mutableSetOf() }.add(transition)

        inputSizes[transition.input.size] = (inputSizes[transition.input.size] ?: 0) + 1
        stackPreviewSizes[transition.stackPreview.size] = (stackPreviewSizes[transition.stackPreview.size] ?: 0) + 1

        return transition
    }

    fun removeTransition(transition: Transition<N>): Transition<N> {
        internalTransitionTable[transition.source]?.remove(transition)

        inputSizes[transition.input.size] = inputSizes.getValue(transition.input.size) - 1
        stackPreviewSizes[transition.stackPreview.size] = stackPreviewSizes.getValue(transition.stackPreview.size) - 1

        return transition
    }

    @Throws(ReachableStateRemoveAttemptException::class)
    fun setInitState(state: State) {
        if (!hasInTransitions(initState)) {
            val oldInitState = initState
            internalInitState = state
            removeState(oldInitState)
        } else {
            throw ReachableStateRemoveAttemptException(initState)
        }
    }

    @Throws(UnreachableFinalStateException::class)
    fun addFinalState(state: State) {
        if (isUnreachable(state)) throw UnreachableFinalStateException(state)
        else internalFinalStates.add(state)
    }

    @Throws(ReachableStateRemoveAttemptException::class)
    fun removeState(state: State) {
        if (isUnreachable(state)) internalTransitionTable.remove(state)
        else throw ReachableStateRemoveAttemptException(state)
    }

    @Throws(MultipleInitStatesException::class)
    fun mergeStates(states: Set<State>): State =
        if (states.size > 1) {
            val newState = nextState()
            if (initState in states) {
                throw MultipleInitStatesException()
            }
            if (states.intersect(finalStates).isNotEmpty()) {
                internalFinalStates.add(newState)
            }
            internalTransitionTable[newState] = transitionTable.entries.filter { (key, _) -> key in states }
                .fold(mutableSetOf()) { acc, (_, value) -> acc.apply { addAll(value.map { it.copy(source = newState) }) } }
            internalTransitionTable.putAll(transitionTable
                .mapValues { (_, value) -> value.filter { it.target in states } }
                .filter { it.value.isNotEmpty() }
                .mapValues { (_, value) -> value.map { it.copy(target = newState) }.toMutableSet() })
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
        for (state in transitionTable.keys.toSet()) {
            if (state !in reachable) {
                removeState(state)
                if (state in finalStates) {
                    internalFinalStates.remove(state)
                }
            }
        }
    }
}