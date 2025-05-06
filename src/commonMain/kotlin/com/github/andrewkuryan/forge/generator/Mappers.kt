package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.GrammarSymbol
import com.github.andrewkuryan.BNF.Nonterminal
import com.github.andrewkuryan.BNF.Terminal
import com.github.andrewkuryan.forge.automata.NSASignal
import com.github.andrewkuryan.forge.automata.StackSignal

fun GrammarSymbol.asStackLetter() =
    when (this) {
        is Terminal -> NSASignal.Symbol(value)
        is Nonterminal -> StackSignal.Node(name)
    }