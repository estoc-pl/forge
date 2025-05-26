package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.InputSignal
import com.github.andrewkuryan.forge.automata.StackSignal

fun RegExp.asStackSignal() = StackSignal.Marker(this.toString())

fun GrammarSymbol.asStackSignal(): StackSignal.Preview =
    when (this) {
        is Terminal -> StackSignal.Symbol(value)
        is Nonterminal -> StackSignal.NodeView(name)
        is RegExp -> asStackSignal()
    }

fun NegatableRegexp.asInputSignal(): InputSignal.Unitary =
    when (this) {
        is RegExp.Symbol -> InputSignal.Symbol(value)
        is RegExp.Range -> InputSignal.Range(value)
    }