package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.removeSuffix
import com.github.andrewkuryan.forge.translation.SemanticAction
import com.github.andrewkuryan.forge.translation.SyntaxNode

fun <N : SyntaxNode> NSA<N>.addRollupTransitions(
    rollupTop: StackSlice,
    rollupTarget: StackSignal.Letter,
    semanticAction: SemanticAction<N>?,
    source: State,
    target: State,
    stackPreviews: List<StackSlice>,
) {
    for (stackPreview in stackPreviews) {
        addTransition(
            StackTransition(rollupTop, rollupTarget, semanticAction, source, target, InputSlice.EMPTY, stackPreview)
        )
    }
}

fun <N : SyntaxNode> NSA<N>.addReadTransitions(
    input: InputSlice,
    stackPush: StackSlice,
    source: State,
    target: State,
    stackPreviews: List<StackSlice>,
) {
    for (stackPreview in stackPreviews) {
        addTransition(
            InputTransition(input, stackPush, source, target, InputSlice.EMPTY, stackPreview)
        )
    }
}

fun <N : SyntaxNode> NSA<N>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
    ports: Ports<N>,
    prefixes: Map<Nonterminal, Set<Prefix>>,
) {
    productions.getValue(nonterm).forEach { production ->
        val stackSymbols = production.symbols.map { it.asStackLetter() }
        val (lastState, lastStackPreview) = production.symbols
            .foldIndexed(ports.getEntry(nonterm) to listOf<StackSlice>()) { index, (prevState, prevStack), symbol ->
                when (symbol) {
                    is Terminal -> {
                        val stackPreviews = prevStack.ifEmpty { listOf(StackSlice(stackSymbols.take(index))) }

                        val nextState = nextState()
                        addReadTransitions(
                            InputSlice(listOf(symbol.asInputLetter())),
                            StackSlice(listOf(symbol.asStackLetter())),
                            prevState,
                            nextState,
                            stackPreviews,
                        )
                        nextState to listOf()
                    }

                    is Nonterminal -> {
                        if (index == 0) {
                            ports.mergeEntries(nonterm, symbol)
                        } else {
                            ports.mergeStateToEntry(prevState, symbol)
                        }

                        ports.getExit(symbol) to resolvePrefix(Prefix(nonterm, stackSymbols.take(index)), prefixes)
                            .map { StackSlice(it + stackSymbols[index]) }
                    }
                }
            }
        val stackPreviews = lastStackPreview
            .map { StackSlice(it.value.removeSuffix(stackSymbols)) }
            .ifEmpty { listOf(StackSlice.EMPTY) }
        addRollupTransitions(
            StackSlice(stackSymbols),
            nonterm.asStackLetter(),
            production.action,
            lastState,
            ports.getExit(nonterm),
            stackPreviews,
        )
    }
}

fun <N : SyntaxNode> Grammar<N>.buildNSAParser() = NSA(nodeBuilder).apply {
    val prefixes = collectPrefixes()

    val ports = Ports(this, productions.keys)

    for (nonterm in productions.keys) {
        processNonterm(nonterm, productions, ports, prefixes)
    }

    setInitState(ports.getEntry(startSymbol))

    val acceptState = nextState()
    addTransition(
        InputTransition(
            InputSlice(listOf(InputSignal.EOI)),
            StackSlice.EMPTY,
            ports.getExit(startSymbol),
            acceptState,
            InputSlice.EMPTY,
            StackSlice(listOf(StackSignal.Bottom, startSymbol.asStackLetter())),
        )
    )
    addFinalState(acceptState)
}