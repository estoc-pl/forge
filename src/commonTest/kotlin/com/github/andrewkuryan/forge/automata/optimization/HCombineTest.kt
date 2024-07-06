package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.automata.NSAFormatPattern
import com.github.andrewkuryan.forge.automata.format
import com.github.andrewkuryan.forge.generator.buildNSAParser
import com.github.andrewkuryan.forge.translation.SyntaxNode
import kotlin.test.Test
import kotlin.test.assertEquals

class HCombineTest {

    @Test
    fun `should build optimized NSA for S → A ⏐ B；A → abc；B → abd`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A / B
            A /= 'a'..'b'..'c'
            B /= 'a'..'b'..'d'

            val nsa = buildNSAParser()
            nsa.optimize(listOf(NSA<SyntaxNode>::hCombine))

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S13";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S14" [label=<ε / *<br/>->]
                    	"S4" -> "S2" [label=<ε / ${"$"}A<br/>A → S>]
                    	"S6" -> "S2" [label=<ε / ${"$"}B<br/>B → S>]
                    	"S9" -> "S4" [label=<ε / abc<br/>abc → A>]
                    	"S12" -> "S6" [label=<ε / abd<br/>abd → B>]
                    	"S2" -> "S13" [label=<┴ / ${"$"}S<br/>->]
                    	"S14" -> "S15" [label=<a / *<br/>a>]
                    	"S15" -> "S16" [label=<b / a<br/>b>]
                    	"S16" -> "S9" [label=<c / ab<br/>c>]
                    	"S16" -> "S12" [label=<d / ab<br/>d>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}