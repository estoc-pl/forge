package com.github.andrewkuryan.forge.generator.BNFtoNSA.regular

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSAFormatPattern
import com.github.andrewkuryan.forge.automata.format
import com.github.andrewkuryan.forge.generator.buildNSAParser
import kotlin.test.Test
import kotlin.test.assertEquals

class LeftRecTest {
    @Test
    fun `should build NSA for S → Sa ⏐ b`() {
        grammar {
            S /= S..'a' / 'b'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S5";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S1" [label=<ε / *<br/>->]
                    	"S1" -> "S4" [label=<b / *<br/>b>]
                    	"S2" -> "S3" [label=<a / ${"$"}S<br/>a>]
                    	"S2" -> "S5" [label=<┴ / ${"$"}S<br/>->]
                    	"S3" -> "S2" [label=<ε / Sa<br/>Sa → S>]
                    	"S4" -> "S2" [label=<ε / b<br/>b → S>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → Aa；A → Bb；B → Sc ⏐ d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A..'a'
            A /= B..'b'
            B /= S..'c' / 'd'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S11";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S4" -> "S7" [label=<a / ${"$"}A<br/>a>]
                    	"S7" -> "S2" [label=<ε / Aa<br/>Aa → S>]
                    	"S3" -> "S5" [label=<ε / *<br/>->]
                    	"S6" -> "S8" [label=<b / ${"$"}B<br/>b>]
                    	"S8" -> "S4" [label=<ε / Bb<br/>Bb → A>]
                    	"S5" -> "S1" [label=<ε / *<br/>->]
                    	"S5" -> "S10" [label=<d / *<br/>d>]
                    	"S2" -> "S9" [label=<c / ${"$"}S<br/>c>]
                    	"S2" -> "S11" [label=<┴ / ${"$"}S<br/>->]
                    	"S9" -> "S6" [label=<ε / Sc<br/>Sc → B>]
                    	"S10" -> "S6" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → Sa ⏐ A；A → Bb；B → Sc ⏐ d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= S..'a' / A
            A /= B..'b'
            B /= S..'c' / 'd'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S11";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S1" [label=<ε / *<br/>->]
                    	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S2" -> "S7" [label=<a / ${"$"}S<br/>a>]
                    	"S2" -> "S9" [label=<c / ${"$"}S<br/>c>]
                    	"S2" -> "S11" [label=<┴ / ${"$"}S<br/>->]
                    	"S7" -> "S2" [label=<ε / Sa<br/>Sa → S>]
                    	"S4" -> "S2" [label=<ε / ${"$"}A<br/>A → S>]
                    	"S3" -> "S5" [label=<ε / *<br/>->]
                    	"S6" -> "S8" [label=<b / ${"$"}B<br/>b>]
                    	"S8" -> "S4" [label=<ε / Bb<br/>Bb → A>]
                    	"S5" -> "S1" [label=<ε / *<br/>->]
                    	"S5" -> "S10" [label=<d / *<br/>d>]
                    	"S9" -> "S6" [label=<ε / Sc<br/>Sc → B>]
                    	"S10" -> "S6" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → Ax ⏐ y；A → Sa ⏐ b`() {
        grammar {
            val A by nonterm()

            S /= A..'x' / 'y'
            A /= S..'a' / 'b'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S9";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S1" -> "S6" [label=<y / *<br/>y>]
                    	"S4" -> "S5" [label=<x / ${"$"}A<br/>x>]
                    	"S5" -> "S2" [label=<ε / Ax<br/>Ax → S>]
                    	"S6" -> "S2" [label=<ε / y<br/>y → S>]
                    	"S3" -> "S1" [label=<ε / *<br/>->]
                    	"S3" -> "S8" [label=<b / *<br/>b>]
                    	"S2" -> "S7" [label=<a / ${"$"}S<br/>a>]
                    	"S2" -> "S9" [label=<┴ / ${"$"}S<br/>->]
                    	"S7" -> "S4" [label=<ε / Sa<br/>Sa → A>]
                    	"S8" -> "S4" [label=<ε / b<br/>b → A>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → Ai ⏐ s；A → Sa ⏐ Bb；B → Ac ⏐ d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A..'i' / 's'
            A /= S..'a' / B..'b'
            B /= A..'c' / 'd'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S13";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S1" -> "S8" [label=<s / *<br/>s>]
                    	"S4" -> "S7" [label=<i / ${"$"}A<br/>i>]
                    	"S4" -> "S11" [label=<c / ${"$"}A<br/>c>]
                    	"S7" -> "S2" [label=<ε / Ai<br/>Ai → S>]
                    	"S8" -> "S2" [label=<ε / s<br/>s → S>]
                    	"S3" -> "S1" [label=<ε / *<br/>->]
                    	"S3" -> "S5" [label=<ε / *<br/>->]
                    	"S2" -> "S9" [label=<a / ${"$"}S<br/>a>]
                    	"S2" -> "S13" [label=<┴ / ${"$"}S<br/>->]
                    	"S9" -> "S4" [label=<ε / Sa<br/>Sa → A>]
                    	"S6" -> "S10" [label=<b / ${"$"}B<br/>b>]
                    	"S10" -> "S4" [label=<ε / Bb<br/>Bb → A>]
                    	"S5" -> "S3" [label=<ε / *<br/>->]
                    	"S5" -> "S12" [label=<d / *<br/>d>]
                    	"S11" -> "S6" [label=<ε / Ac<br/>Ac → B>]
                    	"S12" -> "S6" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}