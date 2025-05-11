package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.RegExp
import com.github.andrewkuryan.BNF.SyntaxNode
import com.github.andrewkuryan.forge.automata.*

private fun <N : SyntaxNode> ENSA<N>.addInputTransition(port: Port, signals: List<NSASignal>) {
    addTransition(
        InputTransition(
            InputSlice(signals),
            StackSlice(signals),
            InputSlice.EMPTY, StackSlice.EMPTY,
            port.entry, port.exit,
        )
    )
}

fun <N : SyntaxNode> ENSA<N>.buildDFAParser(port: Port, regexp: RegExp) {
    when (regexp) {
        is RegExp.ε -> addTransition(EmptyTransition(port.entry, port.exit))
        is RegExp.Symbol -> addInputTransition(port, listOf(NSASignal.Symbol(regexp.value)))
        is RegExp.Row -> addInputTransition(port, regexp.value.map { NSASignal.Symbol(it) })
        is RegExp.Range -> addInputTransition(port, listOf(NSASignal.Range(regexp.value)))
        is RegExp.Not -> addInputTransition(
            port,
            listOf(NSASignal.Not(regexp.first.asNSASignal(), regexp.rest.map { it.asNSASignal() }))
        )

        is RegExp.OneOrMore -> {
            val nestedPort = Port(nextState(), nextState())
            buildDFAParser(nestedPort, regexp.value as RegExp)
            addTransition(EmptyTransition(port.entry, nestedPort.entry))
            addTransition(EmptyTransition(nestedPort.exit, port.exit))
            addTransition(EmptyTransition(nestedPort.exit, nestedPort.entry))
        }

        is RegExp.Or -> {
            for (nestedRegexp in listOf(regexp.first, regexp.second) + regexp.rest) {
                val nestedPort = Port(nextState(), nextState())
                buildDFAParser(nestedPort, nestedRegexp as RegExp)
                addTransition(EmptyTransition(port.entry, nestedPort.entry))
                addTransition(EmptyTransition(nestedPort.exit, port.exit))
            }
        }

        is RegExp.Maybe -> {
            val nestedPort = Port(nextState(), nextState())
            buildDFAParser(nestedPort, regexp.value as RegExp)
            addTransition(EmptyTransition(port.entry, nestedPort.entry))
            addTransition(EmptyTransition(nestedPort.exit, port.exit))
            addTransition(EmptyTransition(port.entry, port.exit))
        }
    }
}