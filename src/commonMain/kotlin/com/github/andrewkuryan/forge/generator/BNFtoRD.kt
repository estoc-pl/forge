package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.*

fun <N : SyntaxNode> ENSA<N>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
    ports: Ports<ENSA<N>>,
) {
    productions.getValue(nonterm).forEach { production ->
        val lastStates = production.symbols
            .fold(listOf(ports.getEntry(nonterm) to listOf<StackSignal>())) { prevStates, symbol ->
                prevStates.flatMap { (prevState, currentStack) ->
                    when (symbol) {
                        is Terminal -> {
                            val nextState = nextState()
                            addTransition(
                                InputTransition(
                                    InputSlice(listOf(InputSignal.Symbol(symbol.value))),
                                    InputSlice.EMPTY, StackSlice.EMPTY,
                                    prevState, nextState,
                                )
                            )
                            listOf(nextState to currentStack + StackSignal.Symbol(symbol.value))
                        }

                        is RegExp -> processRegExp(symbol).map { (port, marker) ->
                            val nextStack = marker?.let { currentStack + marker } ?: currentStack
                            port.exit to nextStack
                        }

                        is Nonterminal -> {
                            addTransition(EmptyTransition(prevState, ports.getEntry(symbol)))

                            listOf(ports.getExit(symbol) to currentStack + symbol.asStackSignal())
                        }
                    }
                }
            }

        lastStates.forEach { (lastState, stackPreview) ->
            addTransition(
                StackTransition(
                    StackSlice(stackPreview.reversed()),
                    StackSignal.Node(nonterm.name),
                    production.action,
                    InputSlice.EMPTY, StackSlice.EMPTY,
                    lastState, ports.getExit(nonterm),
                )
            )
        }
    }
}

fun <N : SyntaxNode> Grammar<N>.buildRDParser() = ENSA<N>().apply {
    val ports = Ports(this, productions.keys)

    for (nonterm in productions.keys) {
        processNonterm(nonterm, productions, ports)
    }

    setInitState(ports.getEntry(startSymbol))

    val acceptState = nextState()
    addTransition(
        InputTransition(
            InputSlice(listOf(InputSignal.EOI)),
            InputSlice.EMPTY, StackSlice(listOf(StackSignal.Node(startSymbol.name), StackSignal.Bottom)),
            ports.getExit(startSymbol), acceptState,
        )
    )
    addFinalState(acceptState)

}.removeEmptyTransitions()