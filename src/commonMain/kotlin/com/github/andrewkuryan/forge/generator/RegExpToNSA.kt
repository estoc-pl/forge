package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.AtomicRegexp
import com.github.andrewkuryan.BNF.BaseRegexp
import com.github.andrewkuryan.BNF.RegExp
import com.github.andrewkuryan.forge.parserKit.transition.*
import com.github.andrewkuryan.forge.automata.*

fun <N : Any> ENSA<N, Transition<N>>.processAtomicRegExp(
    regexp: AtomicRegexp,
    marker: StackSignal.Marker?,
): Port {
    val port = Port(nextState(), nextState())
    val inputSignals = when (regexp) {
        is RegExp.Symbol -> listOf(InputSignal.Symbol(regexp.value))
        is RegExp.Row -> regexp.value.map { InputSignal.Symbol(it) }
        is RegExp.Range -> listOf(InputSignal.Range(regexp.value))
        is RegExp.Not -> listOf(InputSignal.Not(regexp.first.asInputSignal(), regexp.rest.map { it.asInputSignal() }))
    }
    addTransition(
        MeaningfulTransition(
            port.entry, port.exit,
            Guard.Input(input = inputSignals, stackPushBefore = listOfNotNull(marker))
        )
    )
    return port
}

fun <N : Any> ENSA<N, Transition<N>>.processBaseRegExp(regexp: BaseRegexp, marker: StackSignal.Marker?): Port =
    when (regexp) {
        is AtomicRegexp -> processAtomicRegExp(regexp, marker)

        is RegExp.Or -> Port(nextState(), nextState()).apply {
            for (option in listOf(regexp.first, regexp.second) + regexp.rest) {
                val itemPort = processBaseRegExp(option, marker)
                addTransition(EmptyTransition(this.entry, itemPort.entry))
                addTransition(EmptyTransition(itemPort.exit, this.exit))
            }
        }

        is RegExp.OneOrMore -> Port(nextState(), nextState()).apply {
            val valueInitialPort = processBaseRegExp(regexp.value, marker)
            val valueCircularPort = processBaseRegExp(regexp.value, null)

            addTransition(EmptyTransition(this.entry, valueInitialPort.entry))
            addTransition(EmptyTransition(valueInitialPort.exit, valueCircularPort.entry))
            addTransition(EmptyTransition(valueCircularPort.exit, valueCircularPort.entry))
            addTransition(EmptyTransition(valueInitialPort.exit, this.exit))
            addTransition(EmptyTransition(valueCircularPort.exit, this.exit))
        }
    }

fun <N : Any> ENSA<N, Transition<N>>.createEPort() =
    Port(nextState(), nextState()).apply {
        addTransition(EmptyTransition(entry, exit))
    }

fun <N : Any> ENSA<N, Transition<N>>.processRegExp(regexp: RegExp): List<Pair<Port, StackSignal.Marker?>> =
    when (regexp) {
        is BaseRegexp -> regexp.asStackSignal().let { marker -> listOf(processBaseRegExp(regexp, marker) to marker) }
        is RegExp.ε -> listOf(createEPort() to null)
        is RegExp.Maybe -> listOf(
            createEPort() to null,
            (regexp.value as RegExp).asStackSignal().let { marker -> processBaseRegExp(regexp.value, marker) to marker }
        )
    }