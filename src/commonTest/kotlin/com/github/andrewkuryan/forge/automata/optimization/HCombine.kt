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
                   node [shape = doublecircle] "S16";
                   node [shape = circle];
                   secret_node [style=invis, shape=point];
                   secret_node -> "S1" [style=bold];
                   	"S5" -> "S6" [label=<ε / A<br/>->]
                	"S1" -> "S17" [label=<ε / *<br/>->]
                	"S6" -> "S2" [label=<ε / A<br/>A → S>]
                	"S8" -> "S9" [label=<ε / B<br/>->]
                	"S9" -> "S2" [label=<ε / B<br/>B → S>]
                	"S12" -> "S5" [label=<ε / abc<br/>abc → A>]
                	"S15" -> "S8" [label=<ε / abd<br/>abd → B>]
                	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                	"S3" -> "S16" [label=<┴ / ${"$"}S<br/>->]
                	"S17" -> "S18" [label=<a / *<br/>a>]
                	"S18" -> "S19" [label=<b / a<br/>b>]
                	"S19" -> "S12" [label=<c / ab<br/>c>]
                	"S19" -> "S15" [label=<d / ab<br/>d>]
                }
            """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build optimized NSA for S → abA ⏐ cbA；A → i`() {
        grammar {
            val A by nonterm()

            S /= 'a'..'b'..A / 'c'..'b'..A
            A /= 'i'

            val nsa = buildNSAParser()
            nsa.optimize(listOf(NSA<SyntaxNode>::hCombine))

            assertEquals(
                """
                digraph {
                   rankdir=LR;
                   node [shape = doublecircle] "S12";
                   node [shape = circle];
                   secret_node [style=invis, shape=point];
                   secret_node -> "S1" [style=bold];
                   	"S1" -> "S7" [label=<a / *<br/>a>]
                	"S1" -> "S9" [label=<c / *<br/>c>]
                	"S7" -> "S8" [label=<b / a<br/>b>]
                	"S5" -> "S6" [label=<ε / bA<br/>->]
                	"S8" -> "S4" [label=<ε / ab<br/>->]
                	"S6" -> "S2" [label=<ε / abA<br/>abA → S>]
                	"S6" -> "S2" [label=<ε / cbA<br/>cbA → S>]
                	"S9" -> "S10" [label=<b / c<br/>b>]
                	"S10" -> "S4" [label=<ε / cb<br/>->]
                	"S4" -> "S11" [label=<i / *<br/>i>]
                	"S11" -> "S5" [label=<ε / i<br/>i → A>]
                	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                	"S3" -> "S12" [label=<┴ / ${"$"}S<br/>->]
                }
            """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}