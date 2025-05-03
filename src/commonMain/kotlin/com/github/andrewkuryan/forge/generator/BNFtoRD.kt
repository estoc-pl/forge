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
                            InputSlice.EMPTY,
                            StackSlice.EMPTY,
                            prevState,
                            nextState,
                        )
                    )

                    nextState
                }

                is Nonterminal -> {
                    addTransition(EmptyTransition(prevState, ports.getEntry(symbol)))

                    ports.getExit(symbol)
                }
            }
        }

        addTransition(
            StackTransition(
                StackSlice(production.symbols.reversed().map { it.asStackLetter() }),
                nonterm.asStackLetter(),
                production.action,
                InputSlice.EMPTY,
                StackSlice.EMPTY,
                lastState,
                ports.getExit(nonterm),
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
            InputSlice.EMPTY,
            StackSlice(listOf(startSymbol.asStackLetter(), StackSignal.Bottom)),
            ports.getExit(startSymbol),
            acceptState,
        )
    )
    addFinalState(acceptState)

    removeEmptyTransitions()
}