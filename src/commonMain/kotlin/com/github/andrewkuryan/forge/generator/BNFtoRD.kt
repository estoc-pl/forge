package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.*

fun <N : SyntaxNode> NSA<N>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
    ports: Ports<N>,
) {
    productions.getValue(nonterm).forEach { production ->
        val lastState = production.symbols.fold(ports.getEntry(nonterm)) { prevState, symbol ->
            when (symbol) {
                is Terminal -> {
                    val nextState = nextState()
                    val signal = NSASignal.Symbol(symbol.value)
                    addTransition(
                        InputTransition(
                            InputSlice(listOf(signal)),
                            StackSlice(listOf(signal)),
                            InputSlice.EMPTY, StackSlice.EMPTY,
                            prevState, nextState,
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
                StackSignal.Node(nonterm.name),
                production.action,
                InputSlice.EMPTY, StackSlice.EMPTY,
                lastState, ports.getExit(nonterm),
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
            InputSlice.EMPTY, StackSlice(listOf(StackSignal.Node(startSymbol.name), StackSignal.Bottom)),
            ports.getExit(startSymbol), acceptState,
        )
    )
    addFinalState(acceptState)

    removeEmptyTransitions()
}