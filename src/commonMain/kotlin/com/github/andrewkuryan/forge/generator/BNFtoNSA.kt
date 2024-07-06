package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.*
import com.github.andrewkuryan.forge.automata.*
import com.github.andrewkuryan.forge.translation.SemanticAction
import com.github.andrewkuryan.forge.translation.SyntaxNode

fun Terminal.asInput() = Input(Signal.Letter(value))

fun GrammarSymbol.asStackLetter() =
    when (this) {
        is Terminal -> StackSignal.Letter(value.toString())
        is Nonterminal -> StackSignal.Letter(name)
    }

fun <N : SyntaxNode> GrammarSymbol.asPush() = Push<N>(asStackLetter())
fun <N : SyntaxNode> StackPreview.asRollup(target: Nonterminal, semanticAction: SemanticAction<N>?) =
    Rollup(target.asStackLetter(), signals, semanticAction)

fun <N : SyntaxNode> NSA<N>.addTransitions(
    source: State,
    target: State,
    input: Input,
    stackPreviews: List<StackPreview>,
    action: StackAction<N>? = null,
) {
    for (stackPreview in stackPreviews) {
        addTransition(Transition(source, target, input, stackPreview, action))
    }
}

fun <N : SyntaxNode> NSA<N>.processNonterm(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
    ports: Map<Nonterminal, Port>,
    prefixes: Map<Nonterminal, List<Prefix>>,
) {
    val port = ports.getValue(nonterm)
    productions.getValue(nonterm).forEach { production ->
        val stackSymbols = production.symbols.map { it.asStackLetter() }
        val (lastState, lastStackPreview) = production.symbols
            .foldIndexed(port.enter to listOf<StackPreview>()) { index, (prevState, prevStack), symbol ->
                val stackPreviews = prevStack.ifEmpty { listOf(StackPreview(stackSymbols.take(index))) }

                when (symbol) {
                    is Terminal -> {
                        val nextState = nextState()
                        addTransitions(prevState, nextState, symbol.asInput(), stackPreviews, symbol.asPush())
                        nextState to listOf()
                    }

                    is Nonterminal -> {
                        val nestedPort = ports.getValue(symbol)
                        addTransitions(prevState, nestedPort.enter, Input.EMPTY, stackPreviews)

                        nestedPort.exit to resolvePrefix(Prefix(nonterm, stackSymbols.take(index)), prefixes)
                            .map { StackPreview(it + stackSymbols[index]) }
                    }
                }
            }
        val stackPreviews = lastStackPreview.ifEmpty { listOf(StackPreview(stackSymbols)) }
        addTransitions(
            lastState, port.exit,
            Input.EMPTY,
            stackPreviews,
            StackPreview(stackSymbols).asRollup(nonterm, production.action)
        )
    }
}

fun <N : SyntaxNode> Grammar<N>.buildNSAParser() = NSA(nodeBuilder).apply {
    val prefixes = collectPrefixes()

    val ports = productions.mapValues { Port(nextState(), nextState()) }

    for (nonterm in ports.keys) {
        processNonterm(nonterm, productions, ports, prefixes)
    }

    val rootPort = ports.getValue(startSymbol)

    setInitState(rootPort.enter)

    val acceptState = nextState()
    addTransition(
        Transition(
            rootPort.exit, acceptState,
            Input(Signal.EOI),
            StackPreview.BOTTOM + startSymbol.asStackLetter()
        )
    )
    addFinalState(acceptState)
}