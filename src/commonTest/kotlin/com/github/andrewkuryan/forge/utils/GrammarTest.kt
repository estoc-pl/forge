package com.github.andrewkuryan.forge.utils

import kotlin.test.assertEquals
import com.github.andrewkuryan.BNF.Grammar
import com.github.andrewkuryan.forgeKit.transition.Guard
import com.github.andrewkuryan.forgeKit.transition.SyntaxNode
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.automata.format.VizFormatter
import com.github.andrewkuryan.forge.automata.format.format

abstract class GrammarTest(val buildNSA: Grammar.() -> NSA<SyntaxNode>) {

    protected fun assertBuilding(grammar: Grammar, getAssertion: (StateProvider) -> NSAAssertion) =
        assertNSA(grammar.buildNSA(), getAssertion)
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
    val finalRefs: List<StateRef>,
    val transitions: Map<StateRef, List<Pair<Guard.Meaningful<SyntaxNode>, StateRef>>>,
    val verbose: Boolean = false,
) {

    constructor(
        initRef: StateRef,
        finalRef: StateRef,
        transitions: Map<StateRef, List<Pair<Guard.Meaningful<SyntaxNode>, StateRef>>>,
        verbose: Boolean = false,
    ) : this(initRef, listOf(finalRef), transitions, verbose)
}

fun assertNSA(nsa: NSA<SyntaxNode>, getAssertion: (StateProvider) -> NSAAssertion) {
    val provider = StateProvider()
    val (initRef, finalRef, transitions, verbose) = getAssertion(provider)

    if (verbose) {
        println(nsa.format(VizFormatter))
    }
    assertEquals(provider.numOfStates, nsa.states.size, "Total number of states does not match the expected")
    nsa.assertTransitions(initRef, finalRef, transitions)
}