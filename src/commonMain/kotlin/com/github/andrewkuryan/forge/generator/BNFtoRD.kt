package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.automata.optimization.applyHopcroft
import com.github.andrewkuryan.forge.automata.optimization.leftFactorize
import com.github.andrewkuryan.forge.automata.optimization.removeEmptyTransitions
import com.github.andrewkuryan.forge.extensions.grammar.ParserGrammar
import com.github.andrewkuryan.forge.extensions.grammar.SemanticAction
import com.github.andrewkuryan.forge.extensions.grammar.SyntaxNode

fun <N : SyntaxNode, P : Production> ENSA<N, Transition<N>>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<P>>,
    ports: Ports<ENSA<N, Transition<N>>>,
    getProductionAction: (P) -> SemanticAction<N>?,
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

                        is RegExp -> processRegExp(symbol)
                            .onEach { (port) -> addTransition(EmptyTransition(prevState, port.entry)) }
                            .map { (port, marker) ->
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

        addTransitions(
            lastStates.map { (lastState, stackPreview) ->
                StackTransition(
                    StackSlice(stackPreview.reversed()),
                    StackSignal.NodeView(nonterm.name),
                    getProductionAction(production),
                    InputSlice.EMPTY, StackSlice.EMPTY,
                    lastState, ports.getExit(nonterm),
                )
            }
        )
    }
}

fun <N : SyntaxNode> ParserGrammar<N>.buildRDParser(): NSA<N> {
    val ensa = ENSA<N, Transition<N>>()

    val ports = Ports(ensa, productions.keys)

    for (nonterm in productions.keys) {
        ensa.processNonterm(nonterm, productions, ports) { it.action }
    }

    return ensa.wrapAndOptimize(ports, startSymbol)
}

fun Grammar.buildRDParser(): NSA<SyntaxNode> {
    val ensa = ENSA<SyntaxNode, Transition<SyntaxNode>>()

    val ports = Ports(ensa, productions.keys)

    for (nonterm in productions.keys) {
        ensa.processNonterm(nonterm, productions, ports) { null }
    }

    return ensa.wrapAndOptimize(ports, startSymbol)
}

private fun <N : SyntaxNode> ENSA<N, Transition<N>>.wrapAndOptimize(
    ports: Ports<ENSA<N, Transition<N>>>,
    startSymbol: Nonterminal,
): NSA<N> {
    setInitState(ports.getEntry(startSymbol))

    val acceptState = nextState()
    addTransition(
        InputTransition(
            InputSlice(listOf(InputSignal.EOI)),
            InputSlice.EMPTY, StackSlice(listOf(StackSignal.NodeView(startSymbol.name), StackSignal.Bottom)),
            ports.getExit(startSymbol), acceptState,
        )
    )
    addFinalState(acceptState)

    return this
        .removeEmptyTransitions()
        .applyHopcroft()
        .leftFactorize()
}