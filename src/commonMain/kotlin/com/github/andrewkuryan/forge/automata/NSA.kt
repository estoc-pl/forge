package com.github.andrewkuryan.forge.automata

data class State(val index: Int) {
    override fun toString() = "S${index}"
}

data class Transition(
    val source: State,
    val target: State,
    val input: Input = Input.EMPTY,
    val stackPreview: StackPreview = StackPreview.ANY,
    val action: StackAction = StackAction.None,
)

class NSA(
    initState: State = State(0),
    finalStates: Set<State> = setOf(),
    transitionTable: Map<State, Set<Transition>> = mapOf(),
) {
    inner class Port(val enter: State, val innerExit: State) {
        val exit: State = nextState()
        private val barriers = mutableMapOf<StackPreview, Transition>()

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

    private var internalInitState = initState
    val initState: State get() = internalInitState

    private val internalFinalStates = finalStates.toMutableSet()
    val finalStates: Set<State> get() = internalFinalStates

    private val internalTransitionTable = transitionTable
        .mapValues { it.value.toMutableSet() }
        .toMutableMap()

    val transitionTable: Map<State, Set<Transition>> get() = internalTransitionTable

    private var stateCount = 1

    fun nextState() = State(stateCount++)

    fun addTransition(transition: Transition): Transition {
        internalTransitionTable.getOrPut(transition.source) { mutableSetOf() }.add(transition)
        return transition
    }

    fun removeTransition(transition: Transition): Transition {
        internalTransitionTable[transition.source]?.remove(transition)
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

    private fun hasInTransitions(state: State) =
        transitionTable.values.flatten().any { it.target == state }

    private fun isUnreachable(state: State) = !hasInTransitions(state) && state != initState
}