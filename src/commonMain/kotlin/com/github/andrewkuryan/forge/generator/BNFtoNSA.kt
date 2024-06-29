package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.*
import com.github.andrewkuryan.forge.automata.*

fun Terminal.asInput() = Input(Letter(value))

fun GrammarSymbol.asStackLetter() =
    when (this) {
        is Terminal -> StackLetter(value.toString())
        is Nonterminal -> StackLetter(name)
    }

fun GrammarSymbol.asPush() = Push(asStackLetter())
fun StackPreview.asRollup(target: Nonterminal) = Rollup(target.asStackLetter(), frames)

fun NSA.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production>>,
    ports: Map<Nonterminal, NontermPort>,
) {
    val port = ports.getValue(nonterm).nsaPort
    productions.getValue(nonterm).forEach { production ->
        val (lastState, stackPreview) = production.fold(port.enter to StackPreview.ANY) { (prevState, prevStack), symbol ->
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
        addTransition(Transition(lastState, port.innerExit, Input.EMPTY, stackPreview, stackPreview.asRollup(nonterm)))
    }
}

data class NontermPort(val nonterm: Nonterminal, val nsaPort: NSA.Port) {

    fun addBarrier(barrier: StackPreview) {
        nsaPort.addBarrier(barrier + nonterm.asStackLetter())
    }
}

fun Grammar.buildNSAParser() = NSA().apply {
    val ports = productions.mapValues { NontermPort(it.key, Port(nextState(), nextState())) }

    for (nonterm in ports.keys) {
        processNonterm(nonterm, productions, ports)
    }

    val rootPort = ports.getValue(startSymbol)

    rootPort.addBarrier(StackPreview(listOf(StackFrame.Botttom)))
    setInitState(rootPort.nsaPort.enter)

    val accept = nextState()
    addTransition(
        Transition(
            rootPort.nsaPort.exit, accept,
            Input(Signal.EOI),
            StackPreview(listOf(StackFrame.Botttom, startSymbol.asStackLetter()))
        )
    )
    addFinalState(accept)
}