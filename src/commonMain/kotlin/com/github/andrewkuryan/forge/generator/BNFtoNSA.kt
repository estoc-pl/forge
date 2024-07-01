package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.translation.SemanticAction
import com.github.andrewkuryan.forge.translation.SyntaxNode

fun Terminal.asInput() = Input(Signal.Letter(value))

fun GrammarSymbol.asStackLetter() =
    when (this) {
        is Terminal -> StackSignal.Letter(value.toString())
        is Nonterminal -> StackSignal.Letter(name)
    }

fun <N : SyntaxNode> GrammarSymbol.asPush() = Push<N>(asStackLetter())
fun <N : SyntaxNode> StackPreview.asRollup(target: Nonterminal, semanticAction: SemanticAction<N>?) =
    Rollup(target.asStackLetter(), signals, semanticAction)

fun <N : SyntaxNode> NSA<N>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
    ports: Map<Nonterminal, NontermPort<*>>,
) {
    val port = ports.getValue(nonterm).nsaPort
    productions.getValue(nonterm).forEach { production ->
        val (lastState, stackPreview) = production.symbols.fold(port.enter to StackPreview.ANY) { (prevState, prevStack), symbol ->
            when (symbol) {
                is Terminal -> {
                    val nextState = nextState()
                    addTransition(Transition(prevState, nextState, symbol.asInput(), prevStack, symbol.asPush()))
                    nextState to (prevStack + symbol.asStackLetter())
                }

                is Nonterminal -> {
                    val nestedPort = ports.getValue(symbol)
                    nestedPort.addBarrier(prevStack)
                    addTransition(Transition(prevState, nestedPort.nsaPort.enter, Input.EMPTY, prevStack))
                    nestedPort.nsaPort.exit to (prevStack + symbol.asStackLetter())
                }
            }
        }
        addTransition(
            Transition(
                lastState, port.innerExit,
                Input.EMPTY,
                stackPreview,
                stackPreview.asRollup(nonterm, production.action),
            )
        )
    }
}

data class NontermPort<N : SyntaxNode>(val nonterm: Nonterminal, val nsaPort: NSA<N>.Port) {

    fun addBarrier(barrier: StackPreview) {
        nsaPort.addBarrier(barrier + nonterm.asStackLetter())
    }
}

fun <N : SyntaxNode> Grammar<N>.buildNSAParser() = NSA(nodeBuilder).apply {
    val ports = productions.mapValues { NontermPort(it.key, Port(nextState(), nextState())) }

    for (nonterm in ports.keys) {
        processNonterm(nonterm, productions, ports)
    }

    val rootPort = ports.getValue(startSymbol)

    rootPort.addBarrier(StackPreview(listOf(StackSignal.Bottom)))
    setInitState(rootPort.nsaPort.enter)

    val accept = nextState()
    addTransition(
        Transition(
            rootPort.nsaPort.exit, accept,
            Input(Signal.EOI),
            StackPreview(listOf(StackSignal.Bottom, startSymbol.asStackLetter()))
        )
    )
    addFinalState(accept)
}