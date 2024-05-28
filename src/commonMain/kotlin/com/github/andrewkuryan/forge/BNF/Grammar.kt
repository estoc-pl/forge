package com.github.andrewkuryan.forge.BNF

import kotlin.reflect.KProperty

sealed class GrammarSymbol

data class Nonterminal(val name: String, val synthetic: Boolean = false) : GrammarSymbol() {
    override fun toString() = name
}

data class Terminal(val value: Char) : GrammarSymbol() {
    override fun toString() = value.toString()
}

typealias Production = List<GrammarSymbol>

class Grammar(
    startSymbol: Nonterminal = S,
    productions: Map<Nonterminal, Set<Production>> = mapOf(startSymbol to mutableSetOf()),
) {
    companion object {
        val S = Nonterminal("S")
    }

    private var internalStartSymbol: Nonterminal = startSymbol
    private val internalProductions: MutableMap<Nonterminal, MutableSet<Production>> = productions
        .mapValues { it.value.toMutableSet() }
        .toMutableMap()

    val startSymbol: Nonterminal
        get() = internalStartSymbol
    val productions: Map<Nonterminal, Set<Production>>
        get() = internalProductions

    inner class NonterminalDelegate {
        operator fun getValue(thisRef: Grammar?, property: KProperty<*>) = Nonterminal(property.name)
    }

    inner class StartDelegate {
        operator fun getValue(thisRef: Grammar?, property: KProperty<*>) =
            Nonterminal(property.name).apply {
                internalStartSymbol = this
                internalProductions[internalStartSymbol] = mutableSetOf()
            }
    }

    fun nonterm() = NonterminalDelegate()
    fun start() = StartDelegate()

    private fun Char.prod() = listOf(Terminal(this))
    private fun CharRange.prod() = listOf(Terminal(first), Terminal(last))
    private fun Nonterminal.prod() = listOf(this)

    private fun Set<Production>.addToFirst(production: Production) = setOf(production + first()) + drop(1)
    private fun Set<Production>.addToLast(production: Production) = take(size - 1).toSet() + setOf(last() + production)


    operator fun Nonterminal.divAssign(nontermProductions: Set<Production>) {
        internalProductions.getOrPut(this) { mutableSetOf() }.addAll(nontermProductions)
    }

    operator fun Nonterminal.divAssign(char: Char) = divAssign(char.prod())
    operator fun Nonterminal.divAssign(range: CharRange) = divAssign(range.prod())
    operator fun Nonterminal.divAssign(nonterm: Nonterminal) = divAssign(nonterm.prod())
    operator fun Nonterminal.divAssign(production: Production) = divAssign(setOf(production))


    operator fun Char.rangeTo(nonterm: Nonterminal) = setOf(prod() + nonterm)
    operator fun Char.rangeTo(productions: Set<Production>) = productions.addToFirst(prod())

    operator fun CharRange.rangeTo(char: Char) = setOf(prod() + Terminal(char))
    operator fun CharRange.rangeTo(nonterm: Nonterminal) = setOf(prod() + nonterm)
    operator fun CharRange.rangeTo(productions: Set<Production>) = productions.addToFirst(prod())

    operator fun Nonterminal.rangeTo(char: Char) = setOf(prod() + Terminal(char))
    operator fun Nonterminal.rangeTo(nonterm: Nonterminal) = setOf(prod() + nonterm)
    operator fun Nonterminal.rangeTo(productions: Set<Production>) = productions.addToFirst(prod())

    operator fun Set<Production>.rangeTo(char: Char) = addToLast(char.prod())
    operator fun Set<Production>.rangeTo(nonterm: Nonterminal) = addToLast(nonterm.prod())
    operator fun Set<Production>.rangeTo(productions: Set<Production>) =
        addToLast(productions.first()) + productions.drop(1)


    operator fun Char.div(char: Char) = setOf(prod(), char.prod())
    operator fun Char.div(nonterm: Nonterminal) = setOf(prod(), nonterm.prod())
    operator fun CharRange.div(char: Char) = setOf(prod(), char.prod())
    operator fun CharRange.div(nonterm: Nonterminal) = setOf(prod(), nonterm.prod())
    operator fun Nonterminal.div(char: Char) = setOf(prod(), char.prod())
    operator fun Nonterminal.div(nonterm: Nonterminal) = setOf(prod(), nonterm.prod())
    operator fun Set<Production>.div(char: Char) = this + setOf(char.prod())
    operator fun Set<Production>.div(nonterm: Nonterminal) = this + setOf(nonterm.prod())

    private fun Set<Production>.format(head: Nonterminal) =
        head.toString() + " ::= " + joinToString(" | ") { it.joinToString(" ") }

    override fun toString() = productions.getValue(startSymbol).format(startSymbol) +
            (productions - startSymbol).entries.joinToString("\n", prefix = "\n") { it.value.format(it.key) }
}

fun grammar(builder: Grammar.() -> Unit) = Grammar().apply(builder)