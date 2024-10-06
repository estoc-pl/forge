package com.github.andrewkuryan.forge.generator.BNFtoNSA.regular

import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.forge.generator.buildNSAParser
import com.github.andrewkuryan.forge.utils.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LeftRecTest {
    @Test
    fun `should build NSA for S → Sa ⏐ b`() {
        grammar {
            S /= S..'a' / 'b'

            val nsa = buildNSAParser()

            with(nsa) {
                assertEquals(5, states.size)
                val s = Array(5) { StateRef() }

                assertTransitions(
                    s[0], s[4],
                    mapOf(
                        s[0] to listOf(read('b', "") to s[1]),
                        s[1] to listOf(rollup("", "b", "S") to s[2]),
                        s[2] to listOf(
                            read('a', "\$S") to s[3],
                            exit("\$S") to s[4]
                        ),
                        s[3] to listOf(rollup("", "Sa", "S") to s[2]),
                        s[4] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(9, states.size)
                val s = Array(9) { StateRef() }

                assertTransitions(
                    s[0], s[8],
                    mapOf(
                        s[0] to listOf(read('d', "") to s[1]),
                        s[1] to listOf(rollup("", "d", "B") to s[2]),
                        s[2] to listOf(read('b', "\$B") to s[3]),
                        s[3] to listOf(rollup("", "Bb", "A") to s[4]),
                        s[4] to listOf(read('a', "\$A") to s[5]),
                        s[5] to listOf(rollup("", "Aa", "S") to s[6]),
                        s[6] to listOf(
                            read('c', "\$S") to s[7],
                            exit("\$S") to s[8]
                        ),
                        s[7] to listOf(rollup("", "Sc", "B") to s[2]),
                        s[8] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(9, states.size)
                val s = Array(9) { StateRef() }

                assertTransitions(
                    s[0], s[8],
                    mapOf(
                        s[0] to listOf(read('d', "") to s[1]),
                        s[1] to listOf(rollup("", "d", "B") to s[2]),
                        s[2] to listOf(read('b', "\$B") to s[3]),
                        s[3] to listOf(rollup("", "Bb", "A") to s[4]),
                        s[4] to listOf(rollup("\$", "A", "S") to s[5]),
                        s[5] to listOf(
                            read('a', "\$S") to s[6],
                            read('c', "\$S") to s[7],
                            exit("\$S") to s[8]
                        ),
                        s[6] to listOf(rollup("", "Sa", "S") to s[5]),
                        s[7] to listOf(rollup("", "Sc", "B") to s[2]),
                        s[8] to listOf()
                    )
                )
            }
        }
    }

    @Test
    fun `should build NSA for S → Ax ⏐ y；A → Sa ⏐ b`() {
        grammar {
            val A by nonterm()

            S /= A..'x' / 'y'
            A /= S..'a' / 'b'

            val nsa = buildNSAParser()

            with(nsa) {
                assertEquals(8, states.size)
                val s = Array(8) { StateRef() }

                assertTransitions(
                    s[0], s[7],
                    mapOf(
                        s[0] to listOf(
                            read('y', "") to s[1],
                            read('b', "") to s[2]
                        ),
                        s[1] to listOf(rollup("", "y", "S") to s[3]),
                        s[3] to listOf(
                            read('a', "\$S") to s[4],
                            exit("\$S") to s[7]
                        ),
                        s[4] to listOf(rollup("", "Sa", "A") to s[5]),
                        s[5] to listOf(read('x', "\$A") to s[6]),
                        s[6] to listOf(rollup("", "Ax", "S") to s[3]),
                        s[2] to listOf(rollup("", "b", "A") to s[5]),
                        s[7] to listOf()
                    )
                )
            }
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

            with(nsa) {
                assertEquals(11, states.size)
                val s = Array(11) { StateRef() }

                assertTransitions(
                    s[0], s[10],
                    mapOf(
                        s[0] to listOf(
                            read('s', "") to s[1],
                            read('d', "") to s[2]
                        ),
                        s[1] to listOf(rollup("", "s", "S") to s[3]),
                        s[3] to listOf(
                            read('a', "\$S") to s[4],
                            exit("\$S") to s[10]
                        ),
                        s[4] to listOf(rollup("", "Sa", "A") to s[5]),
                        s[5] to listOf(
                            read('i', "\$A") to s[6],
                            read('c', "\$A") to s[7]
                        ),
                        s[6] to listOf(rollup("", "Ai", "S") to s[3]),
                        s[7] to listOf(rollup("", "Ac", "B") to s[8]),
                        s[8] to listOf(read('b', "\$B") to s[9]),
                        s[9] to listOf(rollup("", "Bb", "A") to s[5]),
                        s[2] to listOf(rollup("", "d", "B") to s[8]),
                        s[10] to listOf()
                    )
                )
            }
        }
    }
}