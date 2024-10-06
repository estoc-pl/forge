package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.BNF.Grammar
import com.github.andrewkuryan.BNF.SyntaxNode
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.NSAFormatPattern
import com.github.andrewkuryan.forge.automata.format
import kotlin.test.assertEquals

abstract class GrammarTest(val buildNSA: Grammar<SyntaxNode>.() -> NSA<SyntaxNode>) {

    protected fun assertBuilding(grammar: Grammar<SyntaxNode>, getAssertion: (StateProvider) -> NSAAssertion) {
        val nsa = grammar.buildNSA()
        val provider = StateProvider()
        val (initRef, finalRef, transitions, verbose) = getAssertion(provider)

        assertEquals(provider.numOfStates, nsa.states.size)
        if (verbose) {
            println(nsa.format(NSAFormatPattern.VIZ))
        }
        nsa.assertTransitions(initRef, finalRef, transitions)
    }
}

class StateProvider {
    private val states = mutableMapOf<Int, StateRef>()
    private var internalNumOfStates: Int = 0

    val numOfStates: Int get() = internalNumOfStates

    operator fun get(index: Int): StateRef {
        if (index !in states) {
            states[index] = StateRef()
            internalNumOfStates += 1
        }
        return states.getValue(index)
    }
}

data class NSAAssertion(
    val initRef: StateRef,
    val finalRef: StateRef,
    val transitions: Map<StateRef, List<Pair<TransitionBody, StateRef>>>,
    val verbose: Boolean = false,
)