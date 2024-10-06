package com.github.andrewkuryan.forge.generator.regular

import com.github.andrewkuryan.BNF.Grammar
import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.SyntaxNode
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.utils.GrammarTest

abstract class LeftRecTest(buildNSA: Grammar<SyntaxNode>.() -> NSA<SyntaxNode>) : GrammarTest(buildNSA) {

    protected val `S → Sa ⏐ b` = grammar {
        S /= S..'a' / 'b'
    }

    protected val `S → Aa；A → Bb；B → Sc ⏐ d` = grammar {
        val A by nonterm()
        val B by nonterm()

        S /= A..'a'
        A /= B..'b'
        B /= S..'c' / 'd'
    }

    protected val `S → Sa ⏐ A；A → Bb；B → Sc ⏐ d` = grammar {
        val A by nonterm()
        val B by nonterm()

        S /= S..'a' / A
        A /= B..'b'
        B /= S..'c' / 'd'
    }

    protected val `S → Ax ⏐ y；A → Sa ⏐ b` = grammar {
        val A by nonterm()

        S /= A..'x' / 'y'
        A /= S..'a' / 'b'
    }

    protected val `S → Ai ⏐ s；A → Sa ⏐ Bb；B → Ac ⏐ d` = grammar {
        val A by nonterm()
        val B by nonterm()

        S /= A..'i' / 's'
        A /= S..'a' / B..'b'
        B /= A..'c' / 'd'
    }

    abstract fun `should build NSA for S → Sa ⏐ b`()
    abstract fun `should build NSA for S → Aa；A → Bb；B → Sc ⏐ d`()
    abstract fun `should build NSA for S → Sa ⏐ A；A → Bb；B → Sc ⏐ d`()
    abstract fun `should build NSA for S → Ax ⏐ y；A → Sa ⏐ b`()
    abstract fun `should build NSA for S → Ai ⏐ s；A → Sa ⏐ Bb；B → Ac ⏐ d`()
}