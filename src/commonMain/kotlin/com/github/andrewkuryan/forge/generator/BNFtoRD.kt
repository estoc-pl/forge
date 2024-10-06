package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.*

fun <N : SyntaxNode> NSA<N>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
    ports: Ports<N>,
) {
    productions.getValue(nonterm).forEach { production ->
        val lastState = production.symbols.foldIndexed(ports.getEntry(nonterm)) { index, prevState, symbol ->
            when (symbol) {
                is Terminal -> {
                    val nextState = nextState()

                    addTransition(
                        InputTransition(
                            InputSlice(listOf(symbol.asInputLetter())),
                            StackSlice(listOf(symbol.asStackLetter())),
                            prevState,
                            nextState,
                            InputSlice.EMPTY,
                            StackSlice.EMPTY,
                        )
                    )

                    nextState
                }

                is Nonterminal -> {
                    if (index == 0) {
                        ports.mergeEntries(nonterm, symbol)
                    } else {
                        ports.mergeStateToEntry(prevState, symbol)
                    }

                    ports.getExit(symbol)
                }
            }
        }

        addTransition(
            StackTransition(
                StackSlice(production.symbols.map { it.asStackLetter() }),
                nonterm.asStackLetter(),
                production.action,
                lastState,
                ports.getExit(nonterm),
                InputSlice.EMPTY,
                StackSlice.EMPTY,
            )
        )
    }
}

fun <N : SyntaxNode> Grammar<N>.buildRDParser() = NSA<N>().apply {
    val ports = Ports(this, productions.keys)

    for (nonterm in productions.keys) {
        processNonterm(nonterm, productions, ports)
    }

    setInitState(ports.getEntry(startSymbol))

    val acceptState = nextState()
    addTransition(
        InputTransition(
            InputSlice(listOf(InputSignal.EOI)),
            StackSlice.EMPTY,
            ports.getExit(startSymbol),
            acceptState,
            InputSlice.EMPTY,
            StackSlice(listOf(StackSignal.Bottom, startSymbol.asStackLetter())),
        )
    )
    addFinalState(acceptState)
}