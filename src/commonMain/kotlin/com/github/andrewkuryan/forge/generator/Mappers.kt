package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.BaseInputSignal
import com.github.andrewkuryan.forge.automata.InputSignal
import com.github.andrewkuryan.forge.automata.StackSignal

fun GrammarSymbol.asStackSignal(): List<StackSignal> =
    when (this) {
        is Terminal -> listOf(StackSignal.Symbol(value))
        is Nonterminal -> listOf(StackSignal.Node(name))
        is RegExp -> listOf(StackSignal.Marker(this.toString()))
    }

fun NegatableRegexp.asInputSignal(): BaseInputSignal =
    when (this) {
        is RegExp.Symbol -> InputSignal.Symbol(value)
        is RegExp.Range -> InputSignal.Range(value)
    }