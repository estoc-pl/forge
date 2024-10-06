package com.github.andrewkuryan.forge.generator.BNFtoNSA.regular

import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.forge.generator.buildNSAParser
import com.github.andrewkuryan.forge.utils.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LinearGrammarTest {

    @Test
    fun `should build NSA for S → a`() {
        grammar {
            S /= 'a'

            val nsa = buildNSAParser()

            with(nsa) {
                assertEquals(4, states.size)
                val s = Array(4) { StateRef() }

                assertTransitions(
                    s[0], s[3],
                    mapOf(
                        s[0] to listOf(read('a', "") to s[1]),
                        s[1] to listOf(rollup("", "a", "S") to s[2]),
                        s[2] to listOf(exit("\$S") to s[3]),
                        s[3] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(11, states.size)
                val s = Array(11) { StateRef() }

                assertTransitions(
                    s[0], s[10],
                    mapOf(
                        s[0] to listOf(read('a', "") to s[1]),
                        s[1] to listOf(read('b', "") to s[2]),
                        s[2] to listOf(read('c', "") to s[3]),
                        s[3] to listOf(read('d', "c") to s[4]),
                        s[4] to listOf(rollup("", "cd", "C") to s[5]),
                        s[5] to listOf(rollup("\$a", "bC", "B") to s[6]),
                        s[6] to listOf(rollup("\$", "aB", "A") to s[7]),
                        s[7] to listOf(read('s', "\$A") to s[8]),
                        s[8] to listOf(rollup("", "As", "S") to s[9]),
                        s[9] to listOf(exit("\$S") to s[10]),
                        s[10] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(10, states.size)
                val s = Array(10) { StateRef() }

                assertTransitions(
                    s[0], s[9],
                    mapOf(
                        s[0] to listOf(
                            read('a', "") to s[1],
                            read('d', "") to s[6]
                        ),
                        s[1] to listOf(read('b', "a") to s[2]),
                        s[2] to listOf(read('c', "ab") to s[3]),
                        s[3] to listOf(rollup("", "abc", "A") to s[4]),
                        s[4] to listOf(rollup("\$", "A", "S") to s[5]),
                        s[5] to listOf(exit("\$S") to s[9]),
                        s[6] to listOf(read('e', "d") to s[7]),
                        s[7] to listOf(rollup("", "de", "B") to s[8]),
                        s[8] to listOf(rollup("\$", "B", "S") to s[5]),
                        s[9] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(8, states.size)
                val s = Array(8) { StateRef() }

                assertTransitions(
                    s[0], s[7],
                    mapOf(
                        s[0] to listOf(
                            read('a', "") to s[1],
                            read('a', "") to s[1]
                        ),
                        s[1] to listOf(read('c', "") to s[2]),
                        s[2] to listOf(rollup("", "c", "C") to s[3]),
                        s[3] to listOf(
                            rollup("\$", "aC", "A") to s[4],
                            rollup("\$", "bC", "B") to s[5]
                        ),
                        s[4] to listOf(rollup("\$", "A", "S") to s[6]),
                        s[5] to listOf(rollup("\$", "B", "S") to s[6]),
                        s[6] to listOf(exit("\$S") to s[7]),
                        s[7] to listOf()
                    )
                )
            }
        }
    }

    @Test
    fun `should build NSA for S → aA ⏐ bB；A → Сc；B → Cd；C → e`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= 'a'..A / 'b'..B
            A /= C..'c'
            B /= C..'d'
            C /= 'e'

            val nsa = buildNSAParser()

            with(nsa) {
                assertEquals(10, states.size)
                val s = Array(10) { StateRef() }

                assertTransitions(
                    s[0], s[9],
                    mapOf(
                        s[0] to listOf(
                            read('a', "") to s[1],
                            read('b', "") to s[1]
                        ),
                        s[1] to listOf(read('e', "") to s[2]),
                        s[2] to listOf(rollup("", "e", "C") to s[3]),
                        s[3] to listOf(
                            read('c', "\$aC") to s[4],
                            read('d', "\$bC") to s[5],
                        ),
                        s[4] to listOf(rollup("", "Cc", "A") to s[6]),
                        s[5] to listOf(rollup("", "Cd", "B") to s[7]),
                        s[6] to listOf(rollup("\$", "aA", "S") to s[8]),
                        s[7] to listOf(rollup("\$", "bB", "S") to s[8]),
                        s[8] to listOf(exit("\$S") to s[9]),
                        s[9] to listOf()
                    )
                )
            }
        }
    }
}