package com.github.andrewkuryan.forge.generator.BNFtoNSA.regular

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSAFormatPattern
import com.github.andrewkuryan.forge.automata.format
import com.github.andrewkuryan.forge.generator.buildNSAParser
import kotlin.test.Test
import kotlin.test.assertEquals

class RightRecTest {
    @Test
    fun `should build NSA for S → aS ⏐ b`() {
        grammar {
            S /= 'a'..S / 'b'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S6";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S4" [label=<a / *<br/>a>]
                    	"S1" -> "S5" [label=<b / *<br/>b>]
                    	"S2" -> "S3" [label=<ε / aS<br/>->]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S4" -> "S1" [label=<ε / a<br/>->]
                    	"S3" -> "S2" [label=<ε / aS<br/>aS → S>]
                    	"S3" -> "S6" [label=<┴ / ${"$"}S<br/>->]
                    	"S5" -> "S2" [label=<ε / b<br/>b → S>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → aA；A → bB；B → cS ⏐ d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= 'a'..A
            A /= 'b'..B
            B /= 'c'..S / 'd'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S14";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S10" [label=<a / *<br/>a>]
                    	"S5" -> "S6" [label=<ε / aA<br/>->]
                    	"S10" -> "S4" [label=<ε / a<br/>->]
                    	"S6" -> "S2" [label=<ε / aA<br/>aA → S>]
                    	"S4" -> "S11" [label=<b / *<br/>b>]
                    	"S8" -> "S9" [label=<ε / bB<br/>->]
                    	"S11" -> "S7" [label=<ε / b<br/>->]
                    	"S9" -> "S5" [label=<ε / bB<br/>bB → A>]
                    	"S7" -> "S12" [label=<c / *<br/>c>]
                    	"S7" -> "S13" [label=<d / *<br/>d>]
                    	"S2" -> "S3" [label=<ε / cS<br/>->]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S12" -> "S1" [label=<ε / c<br/>->]
                    	"S3" -> "S8" [label=<ε / cS<br/>cS → B>]
                    	"S3" -> "S14" [label=<┴ / ${"$"}S<br/>->]
                    	"S13" -> "S8" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → aS ⏐ A；A → bB；B → cS ⏐ d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= 'a'..S / A
            A /= 'b'..B
            B /= 'c'..S / 'd'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S14";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S10" [label=<a / *<br/>a>]
                    	"S1" -> "S4" [label=<ε / *<br/>->]
                    	"S2" -> "S3" [label=<ε / aS<br/>->]
                    	"S2" -> "S3" [label=<ε / cS<br/>->]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S10" -> "S1" [label=<ε / a<br/>->]
                    	"S3" -> "S2" [label=<ε / aS<br/>aS → S>]
                    	"S3" -> "S8" [label=<ε / cS<br/>cS → B>]
                    	"S3" -> "S14" [label=<┴ / ${"$"}S<br/>->]
                    	"S5" -> "S6" [label=<ε / A<br/>->]
                    	"S6" -> "S2" [label=<ε / A<br/>A → S>]
                    	"S4" -> "S11" [label=<b / *<br/>b>]
                    	"S8" -> "S9" [label=<ε / bB<br/>->]
                    	"S11" -> "S7" [label=<ε / b<br/>->]
                    	"S9" -> "S5" [label=<ε / bB<br/>bB → A>]
                    	"S7" -> "S12" [label=<c / *<br/>c>]
                    	"S7" -> "S13" [label=<d / *<br/>d>]
                    	"S12" -> "S1" [label=<ε / c<br/>->]
                    	"S13" -> "S8" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → xA ⏐ y；A → aS ⏐ b`() {
        grammar {
            val A by nonterm()

            S /= 'x'..A / 'y'
            A /= 'a'..S / 'b'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S11";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S7" [label=<x / *<br/>x>]
                    	"S1" -> "S8" [label=<y / *<br/>y>]
                    	"S5" -> "S6" [label=<ε / xA<br/>->]
                    	"S7" -> "S4" [label=<ε / x<br/>->]
                    	"S6" -> "S2" [label=<ε / xA<br/>xA → S>]
                    	"S8" -> "S2" [label=<ε / y<br/>y → S>]
                    	"S4" -> "S9" [label=<a / *<br/>a>]
                    	"S4" -> "S10" [label=<b / *<br/>b>]
                    	"S2" -> "S3" [label=<ε / aS<br/>->]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S9" -> "S1" [label=<ε / a<br/>->]
                    	"S3" -> "S5" [label=<ε / aS<br/>aS → A>]
                    	"S3" -> "S11" [label=<┴ / ${"$"}S<br/>->]
                    	"S10" -> "S5" [label=<ε / b<br/>b → A>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }

    @Test
    fun `should build NSA for S → iA ⏐ s；A → aS ⏐ bB；B → cA ⏐ d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= 'i'..A / 's'
            A /= 'a'..S / 'b'..B
            B /= 'c'..A / 'd'

            val nsa = buildNSAParser()

            assertEquals(
                """
                    digraph {
                       rankdir=LR;
                       node [shape = doublecircle] "S16";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S10" [label=<i / *<br/>i>]
                    	"S1" -> "S11" [label=<s / *<br/>s>]
                    	"S5" -> "S6" [label=<ε / iA<br/>->]
                    	"S5" -> "S6" [label=<ε / cA<br/>->]
                    	"S10" -> "S4" [label=<ε / i<br/>->]
                    	"S6" -> "S2" [label=<ε / iA<br/>iA → S>]
                    	"S6" -> "S8" [label=<ε / cA<br/>cA → B>]
                    	"S11" -> "S2" [label=<ε / s<br/>s → S>]
                    	"S4" -> "S12" [label=<a / *<br/>a>]
                    	"S4" -> "S13" [label=<b / *<br/>b>]
                    	"S2" -> "S3" [label=<ε / aS<br/>->]
                    	"S2" -> "S3" [label=<ε / ${"$"}S<br/>->]
                    	"S12" -> "S1" [label=<ε / a<br/>->]
                    	"S3" -> "S5" [label=<ε / aS<br/>aS → A>]
                    	"S3" -> "S16" [label=<┴ / ${"$"}S<br/>->]
                    	"S8" -> "S9" [label=<ε / bB<br/>->]
                    	"S13" -> "S7" [label=<ε / b<br/>->]
                    	"S9" -> "S5" [label=<ε / bB<br/>bB → A>]
                    	"S7" -> "S14" [label=<c / *<br/>c>]
                    	"S7" -> "S15" [label=<d / *<br/>d>]
                    	"S14" -> "S4" [label=<ε / c<br/>->]
                    	"S15" -> "S8" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}