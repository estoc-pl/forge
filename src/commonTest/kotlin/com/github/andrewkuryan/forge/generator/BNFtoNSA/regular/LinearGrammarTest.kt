package com.github.andrewkuryan.forge.generator.BNFtoNSA.regular

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSAFormatPattern
import com.github.andrewkuryan.forge.automata.format
import com.github.andrewkuryan.forge.generator.buildNSAParser
import kotlin.test.Test
import kotlin.test.assertEquals

class LinearGrammarTest {

    @Test
    fun `should build NSA for S → a`() {
        grammar {
            S /= 'a'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S4";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<a / *<br/>a>]
                    	"S3" -> "S2" [label=<ε / a<br/>a → S>]
                    	"S2" -> "S4" [label=<┴ / ${"$"}S<br/>->]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → As；A → aB；B → bC；C → cd`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A..'s'
            A /= 'a'..B
            B /= 'b'..C
            C /= 'c'..'d'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S14";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S4" -> "S9" [label=<s / ${"$"}A<br/>s>]
                    	"S9" -> "S2" [label=<ε / As<br/>As → S>]
                    	"S3" -> "S10" [label=<a / *<br/>a>]
                    	"S10" -> "S5" [label=<ε / a<br/>->]
                    	"S6" -> "S4" [label=<ε / ${"$"}aB<br/>aB → A>]
                    	"S5" -> "S11" [label=<b / *<br/>b>]
                    	"S11" -> "S7" [label=<ε / b<br/>->]
                    	"S8" -> "S6" [label=<ε / ${"$"}abC<br/>bC → B>]
                    	"S7" -> "S12" [label=<c / *<br/>c>]
                    	"S12" -> "S13" [label=<d / c<br/>d>]
                    	"S13" -> "S8" [label=<ε / cd<br/>cd → C>]
                    	"S2" -> "S14" [label=<┴ / ${"$"}S<br/>->]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → A ⏐ B；A → abc；B → de`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A / B
            A /= 'a'..'b'..'c'
            B /= 'd'..'e'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S12";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S1" -> "S5" [label=<ε / *<br/>->]
                    	"S4" -> "S2" [label=<ε / ${"$"}A<br/>A → S>]
                    	"S6" -> "S2" [label=<ε / ${"$"}B<br/>B → S>]
                    	"S3" -> "S7" [label=<a / *<br/>a>]
                    	"S7" -> "S8" [label=<b / a<br/>b>]
                    	"S8" -> "S9" [label=<c / ab<br/>c>]
                    	"S9" -> "S4" [label=<ε / abc<br/>abc → A>]
                    	"S5" -> "S10" [label=<d / *<br/>d>]
                    	"S10" -> "S11" [label=<e / d<br/>e>]
                    	"S11" -> "S6" [label=<ε / de<br/>de → B>]
                    	"S2" -> "S12" [label=<┴ / ${"$"}S<br/>->]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → A ⏐ B；A → aС；B → bC；C → c`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A / B
            A /= 'a'..C
            B /= 'b'..C
            C /= 'c'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S12";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S1" -> "S5" [label=<ε / *<br/>->]
                    	"S4" -> "S2" [label=<ε / ${"$"}A<br/>A → S>]
                    	"S6" -> "S2" [label=<ε / ${"$"}B<br/>B → S>]
                    	"S3" -> "S9" [label=<a / *<br/>a>]
                    	"S9" -> "S7" [label=<ε / a<br/>->]
                    	"S8" -> "S4" [label=<ε / ${"$"}aC<br/>aC → A>]
                    	"S8" -> "S6" [label=<ε / ${"$"}bC<br/>bC → B>]
                    	"S5" -> "S10" [label=<b / *<br/>b>]
                    	"S10" -> "S7" [label=<ε / b<br/>->]
                    	"S7" -> "S11" [label=<c / *<br/>c>]
                    	"S11" -> "S8" [label=<ε / c<br/>c → C>]
                    	"S2" -> "S12" [label=<┴ / ${"$"}S<br/>->]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}