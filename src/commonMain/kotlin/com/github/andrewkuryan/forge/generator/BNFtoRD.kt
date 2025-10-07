package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forgeKit.transition.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.automata.optimization.applyHopcroft
import com.github.andrewkuryan.forge.automata.optimization.leftFactorize
import com.github.andrewkuryan.forge.automata.optimization.removeEmptyTransitions

fun <A : Any, P : Production> ENSA<A, Transition<A>>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<P>>,
    ports: Ports<ENSA<A, Transition<A>>>,
    getProductionAction: (P) -> EvaluationRule<A>?,
) {
    productions.getValue(nonterm).forEach { production ->
        val lastStates = production.symbols
            .fold(listOf(ports.getEntry(nonterm) to listOf<StackSignal.Preview>())) { prevStates, symbol ->
                prevStates.flatMap { (prevState, currentStack) ->
                    when (symbol) {
                        is Terminal -> {
                            val nextState = nextState()
                            addTransition(
                                MeaningfulTransition(
                                    prevState, nextState,
                                    Guard.Input(input = listOf(InputSignal.Symbol(symbol.value)))
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
                MeaningfulTransition(
                    lastState, ports.getExit(nonterm),
                    Guard.Stack(
                        rollupTarget = StackSignal.NodeView(nonterm.name),
                        stack = stackPreview.reversed(),
                        semanticAction = getProductionAction(production)?.asSemanticAction()
                    )
                )
            }
        )
    }
}

fun <A : Any> AttributeGrammar<A>.buildRDParser(): NSA<A> {
    val ensa = ENSA<A, Transition<A>>()

    val ports = Ports(ensa, productions.keys)

    for (nonterm in productions.keys) {
        ensa.processNonterm(nonterm, productions, ports) { it.action }
    }

    return ensa.wrapAndOptimize(ports, startSymbol)
}

fun Grammar.buildRDParser(): NSA<EmptyNode> {
    val ensa = ENSA<EmptyNode, Transition<EmptyNode>>()

    val ports = Ports(ensa, productions.keys)

    for (nonterm in productions.keys) {
        ensa.processNonterm(nonterm, productions, ports) { null }
    }

    return ensa.wrapAndOptimize(ports, startSymbol)
}

private fun <N : Any> ENSA<N, Transition<N>>.wrapAndOptimize(
    ports: Ports<ENSA<N, Transition<N>>>,
    startSymbol: Nonterminal,
): NSA<N> {
    setInitState(ports.getEntry(startSymbol))

    val acceptState = nextState()
    addTransition(
        MeaningfulTransition(
            ports.getExit(startSymbol), acceptState,
            Guard.Input(
                input = listOf(InputSignal.EOI),
                stackPreview = listOf(StackSignal.NodeView(startSymbol.name), StackSignal.Bottom),
            )
        )
    )
    addFinalState(acceptState)

    return this
        .removeEmptyTransitions()
        .applyHopcroft()
        .leftFactorize()
}