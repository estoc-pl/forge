package com.github.andrewkuryan.forge.generator.regular

import com.github.andrewkuryan.BNF.Grammar
import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.SyntaxNode
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.utils.GrammarTest

abstract class LinearGrammarTest(buildNSA: Grammar<SyntaxNode>.() -> NSA<SyntaxNode>) : GrammarTest(buildNSA) {

    protected val `S → a` = grammar {
        S /= 'a'
    }

    protected val `S → As；A → aB；B → bC；C → cd` = grammar {
        val A by nonterm()
        val B by nonterm()
        val C by nonterm()

        S /= A..'s'
        A /= 'a'..B
        B /= 'b'..C
        C /= 'c'..'d'
    }

    protected val `S → A ⏐ B；A → abc；B → de` = grammar {
        val A by nonterm()
        val B by nonterm()

        S /= A / B
        A /= 'a'..'b'..'c'
        B /= 'd'..'e'
    }

    protected val `S → A ⏐ B；A → aС；B → bC；C → c` = grammar {
        val A by nonterm()
        val B by nonterm()
        val C by nonterm()

        S /= A / B
        A /= 'a'..C
        B /= 'b'..C
        C /= 'c'
    }

    protected val `S → aA ⏐ bB；A → Сc；B → Cd；C → e` = grammar {
        val A by nonterm()
        val B by nonterm()
        val C by nonterm()

        S /= 'a'..A / 'b'..B
        A /= C..'c'
        B /= C..'d'
        C /= 'e'
    }

    abstract fun `should build NSA for S → a`()
    abstract fun `should build NSA for S → As；A → aB；B → bC；C → cd`()
    abstract fun `should build NSA for S → A ⏐ B；A → abc；B → de`()
    abstract fun `should build NSA for S → A ⏐ B；A → aС；B → bC；C → c`()
    abstract fun `should build NSA for S → aA ⏐ bB；A → Сc；B → Cd；C → e`()
}