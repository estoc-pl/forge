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
                       node [shape = doublecircle] "S5";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S4" [label=<a / *<br/>a>]
                    	"S4" -> "S2" [label=<ε / a<br/>a → S>]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S3" -> "S5" [label=<┴ / ${"$"}S<br/>->]
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
                       node [shape = doublecircle] "S18";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S5" -> "S6" [label=<ε / A<br/>->]
                    	"S1" -> "S4" [label=<ε / *<br/>->]
                    	"S6" -> "S13" [label=<s / A<br/>s>]
                    	"S13" -> "S2" [label=<ε / As<br/>As → S>]
                    	"S4" -> "S14" [label=<a / *<br/>a>]
                    	"S8" -> "S9" [label=<ε / aB<br/>->]
                    	"S14" -> "S7" [label=<ε / a<br/>->]
                    	"S9" -> "S5" [label=<ε / aB<br/>aB → A>]
                    	"S7" -> "S15" [label=<b / *<br/>b>]
                    	"S11" -> "S12" [label=<ε / bC<br/>->]
                    	"S15" -> "S10" [label=<ε / b<br/>->]
                    	"S12" -> "S8" [label=<ε / bC<br/>bC → B>]
                    	"S10" -> "S16" [label=<c / *<br/>c>]
                    	"S16" -> "S17" [label=<d / c<br/>d>]
                    	"S17" -> "S11" [label=<ε / cd<br/>cd → C>]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S3" -> "S18" [label=<┴ / ${"$"}S<br/>->]
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
                       node [shape = doublecircle] "S15";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S5" -> "S6" [label=<ε / A<br/>->]
                    	"S1" -> "S4" [label=<ε / *<br/>->]
                    	"S1" -> "S7" [label=<ε / *<br/>->]
                    	"S6" -> "S2" [label=<ε / A<br/>A → S>]
                    	"S8" -> "S9" [label=<ε / B<br/>->]
                    	"S9" -> "S2" [label=<ε / B<br/>B → S>]
                    	"S4" -> "S10" [label=<a / *<br/>a>]
                    	"S10" -> "S11" [label=<b / a<br/>b>]
                    	"S11" -> "S12" [label=<c / ab<br/>c>]
                    	"S12" -> "S5" [label=<ε / abc<br/>abc → A>]
                    	"S7" -> "S13" [label=<d / *<br/>d>]
                    	"S13" -> "S14" [label=<e / d<br/>e>]
                    	"S14" -> "S8" [label=<ε / de<br/>de → B>]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S3" -> "S15" [label=<┴ / ${"$"}S<br/>->]
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
                       node [shape = doublecircle] "S16";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S5" -> "S6" [label=<ε / A<br/>->]
                    	"S1" -> "S4" [label=<ε / *<br/>->]
                    	"S1" -> "S7" [label=<ε / *<br/>->]
                    	"S6" -> "S2" [label=<ε / A<br/>A → S>]
                    	"S8" -> "S9" [label=<ε / B<br/>->]
                    	"S9" -> "S2" [label=<ε / B<br/>B → S>]
                    	"S4" -> "S13" [label=<a / *<br/>a>]
                    	"S11" -> "S12" [label=<ε / aC<br/>->]
                    	"S11" -> "S12" [label=<ε / bC<br/>->]
                    	"S13" -> "S10" [label=<ε / a<br/>->]
                    	"S12" -> "S5" [label=<ε / aC<br/>aC → A>]
                    	"S12" -> "S8" [label=<ε / bC<br/>bC → B>]
                    	"S7" -> "S14" [label=<b / *<br/>b>]
                    	"S14" -> "S10" [label=<ε / b<br/>->]
                    	"S10" -> "S15" [label=<c / *<br/>c>]
                    	"S15" -> "S11" [label=<ε / c<br/>c → C>]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S3" -> "S16" [label=<┴ / ${"$"}S<br/>->]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}