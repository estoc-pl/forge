package com.github.andrewkuryan.forge.generator

import com.github.andrewkuryan.BNF.*
import com.github.andrewkuryan.forgeKit.StackSignal

data class Prefix(val head: Nonterminal?, val body: List<StackSignal.Preview>)

fun concatPrefixes(prefixes: Iterable<Prefix>): List<StackSignal.Preview> =
    prefixes.fold(listOf()) { acc, it -> it.body + acc }

fun resolveParentPrefixes(
    current: Nonterminal,
    prefixes: Map<Nonterminal, Set<Prefix>>,
    children: Set<Prefix>,
): List<List<StackSignal.Preview>> = prefixes.getValue(current).flatMap { prefix ->
    when {
        prefix.head == null -> listOf(concatPrefixes(children + prefix))
        prefix in children ->
            if (concatPrefixes(children.drop(children.indexOf(prefix))).isEmpty()) listOf()
            else listOf(concatPrefixes(children + prefix))

        else -> resolveParentPrefixes(prefix.head, prefixes, children + prefix)
    }
}

fun resolvePrefix(prefix: Prefix, prefixes: Map<Nonterminal, Set<Prefix>>): Set<List<StackSignal.Preview>> =
    when (prefix.head) {
        null -> setOf(prefix.body)
        else -> resolveParentPrefixes(prefix.head, prefixes, setOf(prefix)).toSet()
    }

fun resolvePrefixes(nonterm: Nonterminal, prefixes: Map<Nonterminal, Set<Prefix>>): Set<List<StackSignal.Preview>> =
    prefixes.getValue(nonterm).flatMap { prefix -> resolvePrefix(prefix, prefixes) }.toSet()

fun Grammar.collectPrefixes(): Map<Nonterminal, Set<Prefix>> =
    productions.keys
        .fold(mapOf(startSymbol to setOf(Prefix(null, listOf(StackSignal.Bottom))))) { result, nonterm ->
            productions.getValue(nonterm).fold(result) { nontermResult, production ->
                production.symbols.fold(nontermResult to listOf<StackSignal.Preview>()) { (prodResult, prefix), symbol ->
                    when (symbol) {
                        is Terminal -> prodResult
                        is RegExp -> prodResult
                        is Nonterminal -> prodResult +
                                (symbol to (prodResult[symbol] ?: emptySet()) + Prefix(nonterm, prefix))
                    } to (prefix + symbol.asStackSignal())
                }.first
            }
        }