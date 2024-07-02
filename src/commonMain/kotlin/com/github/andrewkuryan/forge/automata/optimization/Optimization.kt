package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.Transition
import com.github.andrewkuryan.forge.translation.SyntaxNode

typealias Optimizer<N> = NSA<N>.(List<Transition<N>>) -> List<Transition<N>>

fun <N : SyntaxNode> NSA<N>.optimize(optimizers: List<Optimizer<N>> = listOf(NSA<N>::hCombine)) {
    val queue = mutableListOf(initState)
    val visited = mutableSetOf(initState)

    while (queue.isNotEmpty()) {
        val currentState = queue.removeAt(0)
        val currentTransitions = transitionTable[currentState]

        val newTransitions =
            if (currentTransitions.isNullOrEmpty()) emptyList()
            else optimizers.fold(currentTransitions.toList()) { transitions, optimizer -> optimizer(transitions) }

        for (transition in newTransitions) {
            if (transition.target !in visited) {
                queue.add(transition.target)
                visited.add(transition.target)
            }
        }
    }

    clearUnreachableStates()
}