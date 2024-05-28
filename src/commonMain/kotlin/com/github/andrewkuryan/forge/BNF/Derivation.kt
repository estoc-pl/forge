package com.github.andrewkuryan.forge.BNF

enum class RecursionKind { LEFT, RIGHT, CENTRAL }

sealed class ProductionKind {
    data class Recursion(val kinds: Set<RecursionKind>) : ProductionKind()
    data object Regular : ProductionKind()
}

data class DerivationNode(val production: Production, val children: Map<Int, DerivationNode>) {
    override fun toString(): String = production.joinToString(" ") +
            children.entries.joinToString("", "\n") { "${production[it.key]} => ${it.value}" }

    fun getExpandedProduction(): Production =
        production.foldIndexed(emptyList()) { index, acc, symbol ->
            if (index in children) acc + children.getValue(index).getExpandedProduction()
            else acc + symbol
        }
}

typealias Derivations = Set<DerivationNode>

fun Grammar.getDerivations(
    nonterm: Nonterminal = startSymbol,
    parents: Set<Nonterminal> = setOf(nonterm),
): Pair<Derivations, Map<Nonterminal, Derivations>> = productions.getValue(nonterm)
    .fold(emptySet<DerivationNode>() to mutableMapOf()) { (prevNodes, prevResult), production ->
        production
            .foldIndexed(mapOf<Int, Derivations>() to mapOf<Nonterminal, Derivations>()) { index, acc, symbol ->
                if (symbol is Nonterminal && symbol !in parents) getDerivations(symbol, parents + symbol)
                    .let { (acc.first + (index to it.first)) to (acc.second + it.second) }
                else acc
            }
            .let { (derivations, result) ->
                derivations.entries.fold(listOf<Map<Int, DerivationNode>>()) { acc, entry ->
                    if (acc.isEmpty()) entry.value.map { mapOf(entry.key to it) }
                    else acc.flatMap { prevEntry ->
                        entry.value.map { prevEntry + mapOf(entry.key to it) }.ifEmpty { listOf(prevEntry) }
                    }
                } to result
            }
            .let { (derivations, result) ->
                val nodes =
                    if (derivations.isEmpty()) listOf(DerivationNode(production, emptyMap()))
                    else derivations.map { DerivationNode(production, it) }
                nodes to (result + (nonterm to nodes))
            }
            .let { (nodes, result) ->
                (prevNodes + nodes) to prevResult.apply {
                    result.forEach { (key, value) -> this[key] = getOrElse(key) { emptySet() } + value }
                }
            }
    }

fun Grammar.getGroupedDerivations(): Map<Nonterminal, Map<ProductionKind, Derivations>> =
    getDerivations().second.mapValues { (nonterm, derivations) -> groupNontermDerivations(nonterm, derivations) }

fun groupNontermDerivations(nonterm: Nonterminal, derivations: Derivations): Map<ProductionKind, Derivations> =
    derivations.groupBy { node ->
        node.getExpandedProduction().let { production ->
            production.foldIndexed(emptySet<RecursionKind>()) { index, acc, it ->
                when (it) {
                    nonterm -> when (index) {
                        0 -> acc + RecursionKind.LEFT
                        production.size - 1 -> acc + RecursionKind.RIGHT
                        else -> acc + RecursionKind.CENTRAL
                    }

                    else -> acc
                }
            }
        }.let { if (it.isEmpty()) ProductionKind.Regular else ProductionKind.Recursion(it) }
    }.mapValues { (_, value) -> value.toSet() }
