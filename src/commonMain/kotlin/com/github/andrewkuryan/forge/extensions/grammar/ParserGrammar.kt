package com.github.andrewkuryan.forge.extensions.grammar

import kotlin.reflect.KProperty0
import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forgeKit.transition.SemanticAction
import com.github.andrewkuryan.forgeKit.transition.SemanticFunction
import com.github.andrewkuryan.forgeKit.transition.SemanticHandler
import com.github.andrewkuryan.forgeKit.transition.SyntaxNode

class ParserProduction<N : SyntaxNode>(
    symbols: List<GrammarSymbol>,
    val action: SemanticAction<N>?,
) : Production(symbols) {

    override fun toString() = "${symbols.joinToString(" ")}${action?.let { " (${it.name})" } ?: ""}"
    override fun equals(other: Any?) = other is ParserProduction<*> && super.equals(other) && action == other.action
    override fun hashCode() = 31 * symbols.hashCode() + (action?.hashCode() ?: 0)
}

class ParserGrammar<N : SyntaxNode> : AbstractGrammar<ParserProduction<N>>(Grammar.S) {

    override fun List<GrammarSymbol>.prod() = ParserProduction<N>(this, null)
    override fun ParserProduction<N>.drop(n: Int) = ParserProduction(symbols.drop(n), action)
    override fun ParserProduction<N>.plus(symbol: GrammarSymbol) = ParserProduction(symbols + symbol, action)
    override fun ParserProduction<N>.plus(other: ParserProduction<N>) =
        ParserProduction(symbols + other.symbols, action)

    operator fun Production.invoke(action: SemanticAction<N>) = ParserProduction(symbols, action)
    operator fun Char.invoke(action: SemanticAction<N>) = prod().invoke(action)
    operator fun RegExp.invoke(action: SemanticAction<N>) = prod().invoke(action)
    operator fun CharRange.invoke(action: SemanticAction<N>) = prod().invoke(action)
    operator fun Nonterminal.invoke(action: SemanticAction<N>) = prod().invoke(action)

    operator fun Production.invoke(fn: KProperty0<SemanticHandler<N>>) = invoke(SemanticAction(fn.name, fn.get()))
    operator fun Production.invoke(fn: SemanticFunction<N>) = invoke(SemanticAction(fn.name, fn))
    operator fun Char.invoke(fn: KProperty0<SemanticHandler<N>>) = prod().invoke(fn)
    operator fun Char.invoke(fn: SemanticFunction<N>) = prod().invoke(fn)
    operator fun RegExp.invoke(fn: KProperty0<SemanticHandler<N>>) = prod().invoke(fn)
    operator fun RegExp.invoke(fn: SemanticFunction<N>) = prod().invoke(fn)
    operator fun CharRange.invoke(fn: KProperty0<SemanticHandler<N>>) = prod().invoke(fn)
    operator fun CharRange.invoke(fn: SemanticFunction<N>) = prod().invoke(fn)
    operator fun Nonterminal.invoke(fn: KProperty0<SemanticHandler<N>>) = prod().invoke(fn)
    operator fun Nonterminal.invoke(fn: SemanticFunction<N>) = prod().invoke(fn)
}

fun <N : SyntaxNode> parserGrammar(builder: ParserGrammar<N>.() -> Unit) = ParserGrammar<N>().apply(builder)