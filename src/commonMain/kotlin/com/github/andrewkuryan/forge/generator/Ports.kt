package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.Nonterminal
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.State
import com.github.andrewkuryan.forge.translation.SyntaxNode

class Ports<N : SyntaxNode>(private val nsa: NSA<N>, nonterms: Set<Nonterminal>) {
    private val ports = nonterms.associateWith { Port(nsa.nextState(), nsa.nextState()) }.toMutableMap()
    private val entries = ports.entries.associate { it.value.entry to setOf(it.key) }.toMutableMap()

    fun getEntry(nonterm: Nonterminal) = ports.getValue(nonterm).entry
    fun getExit(nonterm: Nonterminal) = ports.getValue(nonterm).exit

    private fun mergeStates(state1: State, state2: State) {
        if (state1 != state2) {
            val commonState = nsa.mergeStates(setOf(state1, state2))
            nsa.removeStates(setOf(state1, state2))
            val relatedNonterms = entries.getOrElse(state1) { setOf() } + entries.getOrElse(state2) { setOf() }
            for (nonterm in relatedNonterms) {
                ports[nonterm] = ports.getValue(nonterm).copy(entry = commonState)
            }
            entries.remove(state1)
            entries.remove(state2)
            entries[commonState] = relatedNonterms
        }
    }

    fun mergeEntries(nonterm1: Nonterminal, nonterm2: Nonterminal) =
        mergeStates(ports.getValue(nonterm1).entry, ports.getValue(nonterm2).entry)

    fun mergeStateToEntry(state: State, target: Nonterminal) = mergeStates(state, ports.getValue(target).entry)
}

data class Port(val entry: State, val exit: State)