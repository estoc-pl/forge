package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.GrammarSymbol
import com.github.andrewkuryan.forge.BNF.Nonterminal
import com.github.andrewkuryan.forge.BNF.Terminal
import com.github.andrewkuryan.forge.automata.InputSignal
import com.github.andrewkuryan.forge.automata.StackSignal

fun Terminal.asInputLetter() = InputSignal.Letter(value)

fun GrammarSymbol.asStackLetter() =
    when (this) {
        is Terminal -> StackSignal.Letter(value.toString())
        is Nonterminal -> StackSignal.Letter(name)
    }