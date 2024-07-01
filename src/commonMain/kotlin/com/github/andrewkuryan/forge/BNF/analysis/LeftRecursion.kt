package com.github.andrewkuryan.forge.BNF.analysis

import com.github.andrewkuryan.forge.BNF.*
import com.github.andrewkuryan.forge.BNF.ProductionKind.Recursion
import com.github.andrewkuryan.forge.BNF.RecursionKind.LEFT
import com.github.andrewkuryan.forge.translation.SyntaxNode

data class LeftRecursion(val nonterms: Set<Nonterminal>) : Conclusion(
    ConclusionSeverity.WARNING,
    "Left recursion",
    "Nonterminal${if (nonterms.size > 1) "s" else ""} ${nonterms.joinToString(", ")} ${if (nonterms.size > 1) "have" else "has"} left recursion",
    "Left recursion could be incompatible with some kinds of parsers. Use eliminateLeftRec() to build an equivalent grammar without left recursions",
)

fun hasLeftRecursions(derivations: Map<Nonterminal, Map<ProductionKind, Derivations<*>>>): LeftRecursion? =
    derivations
        .filter { (_, value) -> value.any { (prodKind, _) -> prodKind is Recursion && LEFT in prodKind.kinds } }
        .keys.takeIf { it.isNotEmpty() }?.let { LeftRecursion(it) }

fun <N : SyntaxNode> expandLeftRecProductions(
    nonterm: Nonterminal,
    target: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
): Map<Nonterminal, Set<Production<N>>> =
    productions.getValue(nonterm).fold(emptySet<Production<N>>()) { newProductions, production ->
        if (production.first() == target) newProductions + productions.getValue(target).map { it + production.drop(1) }
        else newProductions + setOf(production)
    }.let { productions + (nonterm to it) }

fun <N : SyntaxNode> eliminateNontermLeftRec(
    nonterm: Nonterminal,
    productions: Map<Nonterminal, Set<Production<N>>>,
): Map<Nonterminal, Set<Production<N>>> =
    productions.getValue(nonterm)
        .fold(emptySet<Production<N>>() to emptySet<Production<N>>()) { (alphas, betas), production ->
            if (production.first() == nonterm) (alphas + setOf(production.drop(1))) to betas
            else alphas to (betas + setOf(production))
        }.let { (alphas, betas) ->
            if (alphas.isEmpty()) productions
            else Nonterminal(nonterm.name, nonterm).let { newNonterm ->
                productions +
                        (nonterm to (betas + betas.map { it + newNonterm }).ifEmpty { setOf(Production(listOf(newNonterm))) }) +
                        (newNonterm to (alphas + alphas.map { it + newNonterm }))
            }
        }

fun <N : SyntaxNode> Grammar<N>.eliminateLeftRec(): Grammar<N> {
    val newProductions =
        productions.keys.fold(setOf<Nonterminal>() to productions) { (processing, outerProductions), outerNonterm ->
            (processing + outerNonterm) to processing
                .fold(outerProductions) { innerProductions, innerNonterm ->
                    expandLeftRecProductions(outerNonterm, innerNonterm, innerProductions)
                }.let { eliminateNontermLeftRec(outerNonterm, it) }
        }.second

    return Grammar(nodeBuilder, startSymbol, newProductions)
}