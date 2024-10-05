package com.github.andrewkuryan.forge.generator.regular

import com.github.andrewkuryan.forge.BNF.Grammar
import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.translation.SyntaxNode
import com.github.andrewkuryan.forge.utils.GrammarTest

abstract class RightRecTest(buildNSA: Grammar<SyntaxNode>.() -> NSA<SyntaxNode>) : GrammarTest(buildNSA) {

    protected val `S → aS ⏐ b` = grammar {
        S /= 'a'..S / 'b'
    }

    protected val `S → aA；A → bB；B → cS ⏐ d` = grammar {
        val A by nonterm()
        val B by nonterm()

        S /= 'a'..A
        A /= 'b'..B
        B /= 'c'..S / 'd'
    }

    protected val `S → aS ⏐ A；A → bB；B → cS ⏐ d` = grammar {
        val A by nonterm()
        val B by nonterm()

        S /= 'a'..S / A
        A /= 'b'..B
        B /= 'c'..S / 'd'
    }

    protected val `S → xA ⏐ y；A → aS ⏐ b` = grammar {
        val A by nonterm()

        S /= 'x'..A / 'y'
        A /= 'a'..S / 'b'
    }

    protected val `S → iA ⏐ s；A → aS ⏐ bB；B → cA ⏐ d` = grammar {
        val A by nonterm()
        val B by nonterm()

        S /= 'i'..A / 's'
        A /= 'a'..S / 'b'..B
        B /= 'c'..A / 'd'
    }

    abstract fun `should build NSA for S → aS ⏐ b`()
    abstract fun `should build NSA for S → aA；A → bB；B → cS ⏐ d`()
    abstract fun `should build NSA for S → aS ⏐ A；A → bB；B → cS ⏐ d`()
    abstract fun `should build NSA for S → xA ⏐ y；A → aS ⏐ b`()
    abstract fun `should build NSA for S → iA ⏐ s；A → aS ⏐ bB；B → cA ⏐ d`()
}