package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.Grammar
import com.github.andrewkuryan.forge.BNF.Nonterminal
import com.github.andrewkuryan.forge.BNF.Terminal
import com.github.andrewkuryan.forge.automata.StackPreview
import com.github.andrewkuryan.forge.automata.StackSignal
import com.github.andrewkuryan.forge.translation.SyntaxNode

data class Prefix(val head: Nonterminal?, val body: List<StackSignal>)

fun resolveParentPrefixes(
    current: Nonterminal,
    prefixes: Map<Nonterminal, List<Prefix>>,
    parents: Set<Prefix>,
): List<List<StackSignal>> = prefixes.getValue(current).flatMap { prefix ->
    when {
        prefix.head == null -> listOf(prefix.body)
        prefix in parents -> if (prefix.body.isEmpty()) listOf() else listOf(listOf())
        else -> resolveParentPrefixes(prefix.head, prefixes, parents + prefix).map { it + prefix.body }
    }
}

fun resolvePrefix(prefix: Prefix, prefixes: Map<Nonterminal, List<Prefix>>): Set<List<StackSignal>> =
    when (prefix.head) {
        null -> setOf(prefix.body)
        else -> resolveParentPrefixes(prefix.head, prefixes, setOf(prefix)).map { it + prefix.body }.toSet()
    }

fun resolvePrefixes(nonterm: Nonterminal, prefixes: Map<Nonterminal, List<Prefix>>): Set<List<StackSignal>> =
    prefixes.getValue(nonterm).flatMap { prefix -> resolvePrefix(prefix, prefixes) }.toSet()

fun <N : SyntaxNode> Grammar<N>.collectPrefixes(): Map<Nonterminal, List<Prefix>> =
    productions.keys
        .fold(mapOf(startSymbol to listOf(Prefix(null, StackPreview.BOTTOM.signals)))) { result, nonterm ->
            productions.getValue(nonterm).fold(result) { nontermResult, production ->
                production.symbols.fold(nontermResult to listOf<StackSignal>()) { (prodResult, prefix), symbol ->
                    when (symbol) {
                        is Terminal -> prodResult
                        is Nonterminal -> prodResult +
                                (symbol to (prodResult[symbol] ?: emptyList()) + Prefix(nonterm, prefix))
                    } to (prefix + symbol.asStackLetter())
                }.first
            }
        }