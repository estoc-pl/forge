package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forge.automata.BaseNSASignal
import com.github.andrewkuryan.forge.automata.NSASignal
import com.github.andrewkuryan.forge.automata.StackSignal

fun GrammarSymbol.asStackLetter() =
    when (this) {
        is Terminal -> NSASignal.Symbol(value)
        is Nonterminal -> StackSignal.Node(name)
    }

fun NegatableRegexp.asNSASignal(): BaseNSASignal =
    when (this) {
        is RegExp.Symbol -> NSASignal.Symbol(value)
        is RegExp.Range -> NSASignal.Range(value)
    }