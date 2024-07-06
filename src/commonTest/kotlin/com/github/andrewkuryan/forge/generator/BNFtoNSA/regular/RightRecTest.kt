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
                       node [shape = doublecircle] "S5";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S3" [label=<a / *<br/>a>]
                    	"S1" -> "S4" [label=<b / *<br/>b>]
                    	"S3" -> "S1" [label=<ε / a<br/>->]
                    	"S2" -> "S2" [label=<ε / ${"$"}aS<br/>aS → S>]
                    	"S2" -> "S2" [label=<ε / aS<br/>aS → S>]
                    	"S2" -> "S5" [label=<┴ / ${"$"}S<br/>->]
                    	"S4" -> "S2" [label=<ε / b<br/>b → S>]
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
                       node [shape = doublecircle] "S11";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S7" [label=<a / *<br/>a>]
                    	"S7" -> "S3" [label=<ε / a<br/>->]
                    	"S4" -> "S2" [label=<ε / ${"$"}aA<br/>aA → S>]
                    	"S4" -> "S2" [label=<ε / bcaA<br/>aA → S>]
                    	"S3" -> "S8" [label=<b / *<br/>b>]
                    	"S8" -> "S5" [label=<ε / b<br/>->]
                    	"S6" -> "S4" [label=<ε / ${"$"}abB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / cabB<br/>bB → A>]
                    	"S5" -> "S9" [label=<c / *<br/>c>]
                    	"S5" -> "S10" [label=<d / *<br/>d>]
                    	"S9" -> "S1" [label=<ε / c<br/>->]
                    	"S2" -> "S6" [label=<ε / ${"$"}abcS<br/>cS → B>]
                    	"S2" -> "S6" [label=<ε / abcS<br/>cS → B>]
                    	"S2" -> "S11" [label=<┴ / ${"$"}S<br/>->]
                    	"S10" -> "S6" [label=<ε / d<br/>d → B>]
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
                       node [shape = doublecircle] "S11";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S7" [label=<a / *<br/>a>]
                    	"S1" -> "S3" [label=<ε / *<br/>->]
                    	"S7" -> "S1" [label=<ε / a<br/>->]
                    	"S2" -> "S2" [label=<ε / ${"$"}aS<br/>aS → S>]
                    	"S2" -> "S2" [label=<ε / aS<br/>aS → S>]
                    	"S2" -> "S2" [label=<ε / ${"$"}bcaS<br/>aS → S>]
                    	"S2" -> "S2" [label=<ε / bcaS<br/>aS → S>]
                    	"S2" -> "S6" [label=<ε / ${"$"}bcS<br/>cS → B>]
                    	"S2" -> "S6" [label=<ε / ${"$"}abcS<br/>cS → B>]
                    	"S2" -> "S6" [label=<ε / abcS<br/>cS → B>]
                    	"S2" -> "S6" [label=<ε / bcS<br/>cS → B>]
                    	"S2" -> "S11" [label=<┴ / ${"$"}S<br/>->]
                    	"S4" -> "S2" [label=<ε / ${"$"}A<br/>A → S>]
                    	"S4" -> "S2" [label=<ε / ${"$"}aA<br/>A → S>]
                    	"S4" -> "S2" [label=<ε / aA<br/>A → S>]
                    	"S3" -> "S8" [label=<b / *<br/>b>]
                    	"S8" -> "S5" [label=<ε / b<br/>->]
                    	"S6" -> "S4" [label=<ε / ${"$"}bB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / ${"$"}abB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / abB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / cabB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / cbB<br/>bB → A>]
                    	"S5" -> "S9" [label=<c / *<br/>c>]
                    	"S5" -> "S10" [label=<d / *<br/>d>]
                    	"S9" -> "S1" [label=<ε / c<br/>->]
                    	"S10" -> "S6" [label=<ε / d<br/>d → B>]
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
                       node [shape = doublecircle] "S9";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S5" [label=<x / *<br/>x>]
                    	"S1" -> "S6" [label=<y / *<br/>y>]
                    	"S5" -> "S3" [label=<ε / x<br/>->]
                    	"S4" -> "S2" [label=<ε / ${"$"}xA<br/>xA → S>]
                    	"S4" -> "S2" [label=<ε / axA<br/>xA → S>]
                    	"S6" -> "S2" [label=<ε / y<br/>y → S>]
                    	"S3" -> "S7" [label=<a / *<br/>a>]
                    	"S3" -> "S8" [label=<b / *<br/>b>]
                    	"S7" -> "S1" [label=<ε / a<br/>->]
                    	"S2" -> "S4" [label=<ε / ${"$"}xaS<br/>aS → A>]
                    	"S2" -> "S4" [label=<ε / xaS<br/>aS → A>]
                    	"S2" -> "S9" [label=<┴ / ${"$"}S<br/>->]
                    	"S8" -> "S4" [label=<ε / b<br/>b → A>]
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
                       node [shape = doublecircle] "S13";
                       node [shape = circle];
                       secret_node [style=invis, shape=point];
                       secret_node -> "S1" [style=bold];
                       	"S1" -> "S7" [label=<i / *<br/>i>]
                    	"S1" -> "S8" [label=<s / *<br/>s>]
                    	"S7" -> "S3" [label=<ε / i<br/>->]
                    	"S4" -> "S2" [label=<ε / ${"$"}iA<br/>iA → S>]
                    	"S4" -> "S2" [label=<ε / aiA<br/>iA → S>]
                    	"S4" -> "S2" [label=<ε / bcaiA<br/>iA → S>]
                    	"S4" -> "S6" [label=<ε / ${"$"}ibcA<br/>cA → B>]
                    	"S4" -> "S6" [label=<ε / aibcA<br/>cA → B>]
                    	"S4" -> "S6" [label=<ε / bcA<br/>cA → B>]
                    	"S8" -> "S2" [label=<ε / s<br/>s → S>]
                    	"S3" -> "S9" [label=<a / *<br/>a>]
                    	"S3" -> "S10" [label=<b / *<br/>b>]
                    	"S9" -> "S1" [label=<ε / a<br/>->]
                    	"S2" -> "S4" [label=<ε / ${"$"}iaS<br/>aS → A>]
                    	"S2" -> "S4" [label=<ε / iaS<br/>aS → A>]
                    	"S2" -> "S4" [label=<ε / ${"$"}ibcaS<br/>aS → A>]
                    	"S2" -> "S4" [label=<ε / ibcaS<br/>aS → A>]
                    	"S2" -> "S4" [label=<ε / bcaS<br/>aS → A>]
                    	"S2" -> "S13" [label=<┴ / ${"$"}S<br/>->]
                    	"S10" -> "S5" [label=<ε / b<br/>->]
                    	"S6" -> "S4" [label=<ε / ${"$"}ibB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / aibB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / caibB<br/>bB → A>]
                    	"S6" -> "S4" [label=<ε / cbB<br/>bB → A>]
                    	"S5" -> "S11" [label=<c / *<br/>c>]
                    	"S5" -> "S12" [label=<d / *<br/>d>]
                    	"S11" -> "S3" [label=<ε / c<br/>->]
                    	"S12" -> "S6" [label=<ε / d<br/>d → B>]
                    }
                """.trimIndent(), nsa.format(NSAFormatPattern.VIZ)
            )
        }
    }
}