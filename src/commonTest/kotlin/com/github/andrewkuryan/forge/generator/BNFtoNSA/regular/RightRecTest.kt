package com.github.andrewkuryan.forge.generator.BNFtoNSA.regular

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.generator.buildNSAParser
import com.github.andrewkuryan.forge.utils.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RightRecTest {
    @Test
    fun `should build NSA for S → aS ⏐ b`() {
        grammar {
            S /= 'a'..S / 'b'

            val nsa = buildNSAParser()

            with(nsa) {
                assertEquals(4, states.size)
                val s = Array(4) { StateRef() }

                assertTransitions(
                    s[0], s[3],
                    mapOf(
                        s[0] to listOf(
                            read('a', "") to s[0],
                            read('b', "") to s[1]
                        ),
                        s[1] to listOf(rollup("b", "b", "S") to s[2]),
                        s[2] to listOf(
                            rollup("\$aS", "aS", "S") to s[2],
                            rollup("aS", "aS", "S") to s[2],
                            exit("\$S") to s[3],
                        ),
                        s[3] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(8, states.size)
                val s = Array(8) { StateRef() }

                assertTransitions(
                    s[0], s[7],
                    mapOf(
                        s[0] to listOf(read('a', "") to s[1]),
                        s[1] to listOf(read('b', "") to s[2]),
                        s[2] to listOf(
                            read('c', "") to s[0],
                            read('d', "") to s[3],
                        ),
                        s[3] to listOf(rollup("d", "d", "B") to s[4]),
                        s[4] to listOf(
                            rollup("\$abB", "bB", "A") to s[5],
                            rollup("cabB", "bB", "A") to s[5]
                        ),
                        s[5] to listOf(
                            rollup("\$aA", "aA", "S") to s[6],
                            rollup("bcaA", "aA", "S") to s[6],
                        ),
                        s[6] to listOf(
                            rollup("\$abcS", "cS", "B") to s[4],
                            rollup("abcS", "cS", "B") to s[4],
                            exit("\$S") to s[7]
                        ),
                        s[7] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(7, states.size)
                val s = Array(7) { StateRef() }

                assertTransitions(
                    s[0], s[6],
                    mapOf(
                        s[0] to listOf(
                            read('a', "") to s[0],
                            read('b', "") to s[1],
                        ),
                        s[1] to listOf(
                            read('c', "") to s[0],
                            read('d', "") to s[2],
                        ),
                        s[2] to listOf(rollup("d", "d", "B") to s[3]),
                        s[3] to listOf(
                            rollup("\$bB", "bB", "A") to s[4],
                            rollup("\$abB", "bB", "A") to s[4],
                            rollup("abB", "bB", "A") to s[4],
                            rollup("cabB", "bB", "A") to s[4],
                            rollup("cbB", "bB", "A") to s[4],
                        ),
                        s[4] to listOf(
                            rollup("\$A", "A", "S") to s[5],
                            rollup("\$aA", "A", "S") to s[5],
                            rollup("aA", "A", "S") to s[5],
                            rollup("bcaA", "A", "S") to s[5],
                            rollup("bcA", "A", "S") to s[5],
                        ),
                        s[5] to listOf(
                            rollup("\$aS", "aS", "S") to s[5],
                            rollup("aS", "aS", "S") to s[5],
                            rollup("\$bcaS", "aS", "S") to s[5],
                            rollup("bcaS", "aS", "S") to s[5],
                            rollup("\$bcS", "cS", "B") to s[3],
                            rollup("\$abcS", "cS", "B") to s[3],
                            rollup("abcS", "cS", "B") to s[3],
                            rollup("bcS", "cS", "B") to s[3],
                            exit("\$S") to s[6]
                        ),
                        s[6] to listOf()
                    )
                )
            }
        }
    }

    @Test
    fun `should build NSA for S → xA ⏐ y；A → aS ⏐ b`() {
        grammar {
            val A by nonterm()

            S /= 'x'..A / 'y'
            A /= 'a'..S / 'b'

            val nsa = buildNSAParser()

            with(nsa) {
                assertEquals(7, states.size)
                val s = Array(7) { StateRef() }

                assertTransitions(
                    s[0], s[6],
                    mapOf(
                        s[0] to listOf(
                            read('x', "") to s[1],
                            read('y', "") to s[2]
                        ),
                        s[1] to listOf(
                            read('a', "") to s[0],
                            read('b', "") to s[3]
                        ),
                        s[3] to listOf(rollup("b", "b", "A") to s[4]),
                        s[4] to listOf(
                            rollup("\$xA", "xA", "S") to s[5],
                            rollup("axA", "xA", "S") to s[5],
                        ),
                        s[5] to listOf(
                            rollup("\$xaS", "aS", "A") to s[4],
                            rollup("xaS", "aS", "A") to s[4],
                            exit("\$S") to s[6]
                        ),
                        s[2] to listOf(rollup("y", "y", "S") to s[5]),
                        s[6] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(9, states.size)
                val s = Array(9) { StateRef() }

                assertTransitions(
                    s[0], s[8],
                    mapOf(
                        s[0] to listOf(
                            read('i', "") to s[1],
                            read('s', "") to s[2]
                        ),
                        s[1] to listOf(
                            read('a', "") to s[0],
                            read('b', "") to s[3]
                        ),
                        s[3] to listOf(
                            read('c', "") to s[1],
                            read('d', "") to s[4]
                        ),
                        s[4] to listOf(rollup("d", "d", "B") to s[5]),
                        s[5] to listOf(
                            rollup("\$ibB", "bB", "A") to s[6],
                            rollup("aibB", "bB", "A") to s[6],
                            rollup("caibB", "bB", "A") to s[6],
                            rollup("cbB", "bB", "A") to s[6],
                        ),
                        s[6] to listOf(
                            rollup("\$iA", "iA", "S") to s[7],
                            rollup("aiA", "iA", "S") to s[7],
                            rollup("bcaiA", "iA", "S") to s[7],
                            rollup("\$ibcA", "cA", "B") to s[5],
                            rollup("aibcA", "cA", "B") to s[5],
                            rollup("bcA", "cA", "B") to s[5],
                        ),
                        s[7] to listOf(
                            rollup("\$iaS", "aS", "A") to s[6],
                            rollup("iaS", "aS", "A") to s[6],
                            rollup("\$ibcaS", "aS", "A") to s[6],
                            rollup("ibcaS", "aS", "A") to s[6],
                            rollup("bcaS", "aS", "A") to s[6],
                            exit("\$S") to s[8]
                        ),
                        s[2] to listOf(rollup("s", "s", "S") to s[7]),
                        s[8] to listOf()
                    )
                )
            }
        }
    }
}