package com.github.andrewkuryan.forge.utils

import com.github.andrewkuryan.BNF.Grammar
import com.github.andrewkuryan.BNF.SyntaxNode
import com.github.andrewkuryan.forge.automata.*
import kotlin.test.assertEquals

abstract class GrammarTest(val buildNSA: Grammar<SyntaxNode>.() -> NSA<SyntaxNode>) {

    protected fun assertBuilding(grammar: Grammar<SyntaxNode>, getAssertion: (StateProvider) -> NSAAssertion) =
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
    val transitions: Map<StateRef, List<Pair<TransitionBody, StateRef>>>,
    val verbose: Boolean = false,
) {

    constructor(
        initRef: StateRef,
        finalRef: StateRef,
        transitions: Map<StateRef, List<Pair<TransitionBody, StateRef>>>,
        verbose: Boolean = false,
    ) : this(initRef, listOf(finalRef), transitions, verbose)
}

fun assertNSA(nsa: NSA<SyntaxNode>, getAssertion: (StateProvider) -> NSAAssertion) {
    val provider = StateProvider()
    val (initRef, finalRef, transitions, verbose) = getAssertion(provider)

    if (verbose) {
        println(nsa.format(NSAFormatPattern.VIZ))
    }
    assertEquals(provider.numOfStates, nsa.states.size, "Total number of states does not match the expected")
    nsa.assertTransitions(initRef, finalRef, transitions)
}