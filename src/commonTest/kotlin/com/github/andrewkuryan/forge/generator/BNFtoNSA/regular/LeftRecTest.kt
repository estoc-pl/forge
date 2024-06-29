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
                       node [shape = doublecircle] "S6";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S2" -> "S3" [label=<ε / S<br/>->]
                    	"S1" -> "S1" [label=<ε / *<br/>->]
                    	"S1" -> "S5" [label=<b / *<br/>b>]
                    	"S3" -> "S4" [label=<a / S<br/>a>]
                    	"S3" -> "S6" [label=<┴ / ${"$"}S<br/>->]
                    	"S4" -> "S2" [label=<ε / Sa<br/>Sa → S>]
                    	"S5" -> "S2" [label=<ε / b<br/>b → S>]
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
                       node [shape = doublecircle] "S14";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S5" -> "S6" [label=<ε / A<br/>->]
                    	"S1" -> "S4" [label=<ε / *<br/>->]
                    	"S6" -> "S10" [label=<a / A<br/>a>]
                    	"S10" -> "S2" [label=<ε / Aa<br/>Aa → S>]
                    	"S8" -> "S9" [label=<ε / B<br/>->]
                    	"S4" -> "S7" [label=<ε / *<br/>->]
                    	"S9" -> "S11" [label=<b / B<br/>b>]
                    	"S11" -> "S5" [label=<ε / Bb<br/>Bb → A>]
                    	"S2" -> "S3" [label=<ε / S<br/>->]
                    	"S7" -> "S1" [label=<ε / *<br/>->]
                    	"S7" -> "S13" [label=<d / *<br/>d>]
                    	"S3" -> "S12" [label=<c / S<br/>c>]
                    	"S3" -> "S14" [label=<┴ / ${"$"}S<br/>->]
                    	"S12" -> "S8" [label=<ε / Sc<br/>Sc → B>]
                    	"S13" -> "S8" [label=<ε / d<br/>d → B>]
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
                   node [shape = doublecircle] "S14";
                   node [shape = circle];
                   secret_node [style=invis, shape=point];
                   secret_node -> "S1" [style=bold];
                   	"S2" -> "S3" [label=<ε / S<br/>->]
                	"S1" -> "S1" [label=<ε / *<br/>->]
                	"S1" -> "S4" [label=<ε / *<br/>->]
                	"S3" -> "S10" [label=<a / S<br/>a>]
                	"S3" -> "S12" [label=<c / S<br/>c>]
                	"S3" -> "S14" [label=<┴ / ${"$"}S<br/>->]
                	"S10" -> "S2" [label=<ε / Sa<br/>Sa → S>]
                	"S5" -> "S6" [label=<ε / A<br/>->]
                	"S6" -> "S2" [label=<ε / A<br/>A → S>]
                	"S8" -> "S9" [label=<ε / B<br/>->]
                	"S4" -> "S7" [label=<ε / *<br/>->]
                	"S9" -> "S11" [label=<b / B<br/>b>]
                	"S11" -> "S5" [label=<ε / Bb<br/>Bb → A>]
                	"S7" -> "S1" [label=<ε / *<br/>->]
                	"S7" -> "S13" [label=<d / *<br/>d>]
                	"S12" -> "S8" [label=<ε / Sc<br/>Sc → B>]
                	"S13" -> "S8" [label=<ε / d<br/>d → B>]
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
                   node [shape = doublecircle] "S11";
                   node [shape = circle];
                   secret_node [style=invis, shape=point];
                   secret_node -> "S1" [style=bold];
                   	"S5" -> "S6" [label=<ε / A<br/>->]
                	"S1" -> "S4" [label=<ε / *<br/>->]
                	"S1" -> "S8" [label=<y / *<br/>y>]
                	"S6" -> "S7" [label=<x / A<br/>x>]
                	"S7" -> "S2" [label=<ε / Ax<br/>Ax → S>]
                	"S8" -> "S2" [label=<ε / y<br/>y → S>]
                	"S2" -> "S3" [label=<ε / S<br/>->]
                	"S4" -> "S1" [label=<ε / *<br/>->]
                	"S4" -> "S10" [label=<b / *<br/>b>]
                	"S3" -> "S9" [label=<a / S<br/>a>]
                	"S3" -> "S11" [label=<┴ / ${"$"}S<br/>->]
                	"S9" -> "S5" [label=<ε / Sa<br/>Sa → A>]
                	"S10" -> "S5" [label=<ε / b<br/>b → A>]
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
                       node [shape = doublecircle] "S16";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S5" -> "S6" [label=<ε / A<br/>->]
                    	"S1" -> "S4" [label=<ε / *<br/>->]
                    	"S1" -> "S11" [label=<s / *<br/>s>]
                    	"S6" -> "S10" [label=<i / A<br/>i>]
                    	"S6" -> "S14" [label=<c / A<br/>c>]
                    	"S10" -> "S2" [label=<ε / Ai<br/>Ai → S>]
                    	"S11" -> "S2" [label=<ε / s<br/>s → S>]
                    	"S2" -> "S3" [label=<ε / S<br/>->]
                    	"S4" -> "S1" [label=<ε / *<br/>->]
                    	"S4" -> "S7" [label=<ε / *<br/>->]
                    	"S3" -> "S12" [label=<a / S<br/>a>]
                    	"S3" -> "S16" [label=<┴ / ${"$"}S<br/>->]
                    	"S12" -> "S5" [label=<ε / Sa<br/>Sa → A>]
                    	"S8" -> "S9" [label=<ε / B<br/>->]
                    	"S9" -> "S13" [label=<b / B<br/>b>]
                    	"S13" -> "S5" [label=<ε / Bb<br/>Bb → A>]
                    	"S7" -> "S4" [label=<ε / *<br/>->]
                    	"S7" -> "S15" [label=<d / *<br/>d>]
                    	"S14" -> "S8" [label=<ε / Ac<br/>Ac → B>]
                    	"S15" -> "S8" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}