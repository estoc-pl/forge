package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forgeKit.transition.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.extensions.removeSuffix

fun NSA<SyntaxNode>.addRollupTransitions(
    rollupTop: StackSlice,
    rollupTarget: StackSignal.NodeView,
    source: State,
    target: State,
    stackPreviews: List<StackSlice>,
) = addTransitions(
    stackPreviews.map {
        MeaningfulTransition(
            source, target,
            Guard.Stack(rollupTarget = rollupTarget, stack = rollupTop, stackPreview = it)
        )
    }
)

fun NSA<SyntaxNode>.addReadTransitions(
    input: InputSlice,
    source: State,
    target: State,
    stackPreviews: List<StackSlice>,
) = addTransitions(
    stackPreviews.map {
        MeaningfulTransition(source, target, Guard.Input(input = input, stackPreview = it))
    }
)

fun NSA<SyntaxNode>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production>>,
    ports: NSAPorts<SyntaxNode>,
    prefixes: Map<Nonterminal, Set<Prefix>>,
) {
    productions.getValue(nonterm).forEach { production ->
        val stackSymbols = production.symbols.map { it.asStackSignal() }
        val (lastState, lastStackPreview) = production.symbols
            .foldIndexed(ports.getEntry(nonterm) to listOf<StackSlice>()) { index, (prevState, prevStack), symbol ->
                when (symbol) {
                    is Terminal -> {
                        val stackPreviews = prevStack.ifEmpty { listOf(stackSymbols.take(index)) }
                        val nextState = nextState()
                        addReadTransitions(
                            listOf(InputSignal.Symbol(symbol.value)),
                            prevState, nextState,
                            stackPreviews,
                        )
                        nextState to listOf()
                    }

                    is RegExp -> TODO("Not implemented")

                    is Nonterminal -> {
                        if (index == 0) {
                            ports.mergeEntries(nonterm, symbol)
                        } else {
                            ports.mergeStateToEntry(prevState, symbol)
                        }

                        ports.getExit(symbol) to resolvePrefix(Prefix(nonterm, stackSymbols.take(index)), prefixes)
                            .map { it + stackSymbols[index] }
                    }
                }
            }
        val stackPreviews = lastStackPreview.map { it.removeSuffix(stackSymbols) }.ifEmpty { listOf(emptyList()) }
        addRollupTransitions(
            stackSymbols,
            StackSignal.NodeView(nonterm.name),
            lastState, ports.getExit(nonterm),
            stackPreviews,
        )
    }
}

fun Grammar.buildNSAParser() = NSA<SyntaxNode>().apply {
    val prefixes = collectPrefixes()

    val ports = NSAPorts(this, productions.keys)

    for (nonterm in productions.keys) {
        processNonterm(nonterm, productions, ports, prefixes)
    }

    setInitState(ports.getEntry(startSymbol))

    val acceptState = nextState()
    addTransition(
        MeaningfulTransition(
            ports.getExit(startSymbol), acceptState,
            Guard.Input(
                input = listOf(InputSignal.EOI),
                stackPreview = listOf(StackSignal.Bottom, StackSignal.NodeView(startSymbol.name))
            )
        )
    )
    addFinalState(acceptState)
}