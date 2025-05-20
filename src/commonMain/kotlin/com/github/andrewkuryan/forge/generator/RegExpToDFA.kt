package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.*

fun <N : SyntaxNode> ENSA<N>.processAtomicRegExp(regexp: AtomicRegexp, marker: StackSignal.Marker?): Port {
    val port = Port(nextState(), nextState())
    val inputSignals = when (regexp) {
        is RegExp.Symbol -> listOf(InputSignal.Symbol(regexp.value))
        is RegExp.Row -> regexp.value.map { InputSignal.Symbol(it) }
        is RegExp.Range -> listOf(InputSignal.Range(regexp.value))
        is RegExp.Not -> listOf(InputSignal.Not(regexp.first.asInputSignal(), regexp.rest.map { it.asInputSignal() }))
    }
    addTransition(
        InputTransition(
            InputSlice(inputSignals),
            InputSlice.EMPTY, StackSlice.EMPTY,
            port.entry, port.exit,
            marker?.let { StackPush(listOf(it)) } ?: StackPush.EMPTY
        )
    )
    return port
}

fun <N : SyntaxNode> ENSA<N>.processBaseRegExp(regexp: BaseRegexp, marker: StackSignal.Marker?): Port {
    return when (regexp) {
        is AtomicRegexp -> processAtomicRegExp(regexp, marker)

        is RegExp.Or -> {
            val port = Port(nextState(), nextState())
            for (option in listOf(regexp.first, regexp.second) + regexp.rest) {
                val itemPort = processBaseRegExp(option, marker)
                addTransition(EmptyTransition(port.entry, itemPort.entry))
                addTransition(EmptyTransition(itemPort.exit, port.exit))
            }
            port
        }

        is RegExp.OneOrMore -> {
            val port = Port(nextState(), nextState())
            val valueInitialPort = processBaseRegExp(regexp.value, marker)
            val valueCircularPort = processBaseRegExp(regexp.value, null)

            addTransition(EmptyTransition(port.entry, valueInitialPort.entry))
            addTransition(EmptyTransition(valueInitialPort.exit, valueCircularPort.entry))
            addTransition(EmptyTransition(valueCircularPort.exit, valueCircularPort.entry))
            addTransition(EmptyTransition(valueInitialPort.exit, port.exit))
            addTransition(EmptyTransition(valueCircularPort.exit, port.exit))

            port
        }
    }
}

fun <N : SyntaxNode> ENSA<N>.createEPort() =
    Port(nextState(), nextState()).apply {
        addTransition(EmptyTransition(entry, exit))
    }

fun <N : SyntaxNode> ENSA<N>.processRegExp(regexp: RegExp): List<Pair<Port, StackSignal.Marker?>> {
    return when (regexp) {
        is BaseRegexp -> StackSignal.Marker(regexp.toString()).let { marker ->
            listOf(processBaseRegExp(regexp, marker) to marker)
        }

        is RegExp.ε -> listOf(createEPort() to null)
        is RegExp.Maybe -> listOf(
            createEPort() to null,
            StackSignal.Marker(regexp.value.toString()).let { marker ->
                processBaseRegExp(regexp.value, marker) to marker
            }
        )
    }
}