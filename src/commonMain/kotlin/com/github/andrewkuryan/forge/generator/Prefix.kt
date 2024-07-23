package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.forge.BNF.Grammar
import com.github.andrewkuryan.forge.BNF.Nonterminal
import com.github.andrewkuryan.forge.BNF.Terminal
import com.github.andrewkuryan.forge.automata.StackPreview
import com.github.andrewkuryan.forge.automata.StackSignal
import com.github.andrewkuryan.forge.translation.SyntaxNode

data class Prefix(val head: Nonterminal?, val body: List<StackSignal>)

fun concatPrefixes(prefixes: Iterable<Prefix>): List<StackSignal> = prefixes.fold(listOf()) { acc, it -> it.body + acc }

fun resolveParentPrefixes(
    current: Nonterminal,
    prefixes: Map<Nonterminal, Set<Prefix>>,
    children: Set<Prefix>,
): List<List<StackSignal>> = prefixes.getValue(current).flatMap { prefix ->
    when {
        prefix.head == null -> listOf(concatPrefixes(children + prefix))
        prefix in children ->
            if (concatPrefixes(children.drop(children.indexOf(prefix))).isEmpty()) listOf()
            else listOf(concatPrefixes(children + prefix))

        else -> resolveParentPrefixes(prefix.head, prefixes, children + prefix)
    }
}

fun resolvePrefix(prefix: Prefix, prefixes: Map<Nonterminal, Set<Prefix>>): Set<List<StackSignal>> =
    when (prefix.head) {
        null -> setOf(prefix.body)
        else -> resolveParentPrefixes(prefix.head, prefixes, setOf(prefix)).toSet()
    }

fun resolvePrefixes(nonterm: Nonterminal, prefixes: Map<Nonterminal, Set<Prefix>>): Set<List<StackSignal>> =
    prefixes.getValue(nonterm).flatMap { prefix -> resolvePrefix(prefix, prefixes) }.toSet()

fun <N : SyntaxNode> Grammar<N>.collectPrefixes(): Map<Nonterminal, Set<Prefix>> =
    productions.keys
        .fold(mapOf(startSymbol to setOf(Prefix(null, StackPreview.BOTTOM.signals)))) { result, nonterm ->
            productions.getValue(nonterm).fold(result) { nontermResult, production ->
                production.symbols.fold(nontermResult to listOf<StackSignal>()) { (prodResult, prefix), symbol ->
                    when (symbol) {
                        is Terminal -> prodResult
                        is Nonterminal -> prodResult +
                                (symbol to (prodResult[symbol] ?: emptySet()) + Prefix(nonterm, prefix))
                    } to (prefix + symbol.asStackLetter())
                }.first
            }
        }