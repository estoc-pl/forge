package com.github.andrewkuryan.forge.automata.optimization

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.automata.NSA
import com.github.andrewkuryan.forge.generator.buildNSAParser
import com.github.andrewkuryan.forge.translation.SyntaxNode
import com.github.andrewkuryan.forge.utils.*
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

            with(nsa) {
                assertEquals(9, states.size)
                val s = Array(9) { StateRef() }

                assertTransitions(
                    s[0], s[8],
                    mapOf(
                        s[0] to listOf(read('a', "") to s[1]),
                        s[1] to listOf(read('b', "a") to s[2]),
                        s[2] to listOf(
                            read('c', "ab") to s[3],
                            read('d', "ab") to s[4]
                        ),
                        s[3] to listOf(rollup("", "abc", "A") to s[5]),
                        s[4] to listOf(rollup("", "abd", "B") to s[6]),
                        s[5] to listOf(rollup("\$", "A", "S") to s[7]),
                        s[6] to listOf(rollup("\$", "B", "S") to s[7]),
                        s[7] to listOf(exit("\$S") to s[8]),
                        s[8] to listOf()
                    )
                )
            }
        }
    }

    @Test
    fun `should build optimized NSA for S → A ⏐ B；A → abc；B → aC；C → b`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A / B
            A /= 'a'..'b'..'c'
            B /= 'a'..C
            C /= 'b'

            val nsa = buildNSAParser()
            nsa.optimize(listOf(NSA<SyntaxNode>::hCombine))

            with(nsa) {
                assertEquals(9, states.size)
                val s = Array(9) { StateRef() }

                assertTransitions(
                    s[0], s[8],
                    mapOf(
                        s[0] to listOf(read('a', "") to s[1]),
                        s[1] to listOf(read('b', "") to s[2]),
                        s[2] to listOf(
                            read('c', "ab") to s[3],
                            rollup("", "b", "C") to s[4]
                        ),
                        s[3] to listOf(rollup("", "abc", "A") to s[5]),
                        s[4] to listOf(rollup("\$", "aC", "B") to s[6]),
                        s[5] to listOf(rollup("\$", "A", "S") to s[7]),
                        s[6] to listOf(rollup("\$", "B", "S") to s[7]),
                        s[7] to listOf(exit("\$S") to s[8]),
                        s[8] to listOf()
                    )
                )
            }
        }
    }

    @Test
    fun `should build optimized NSA for S → Sc ⏐ A ⏐ s；A → sa`() {
        grammar {
            val A by nonterm()

            S /= S..'c' / A / 's'
            A /= 's'..'a'

            val nsa = buildNSAParser()
            nsa.optimize(listOf(NSA<SyntaxNode>::hCombine))

            with(nsa) {
                assertEquals(7, states.size)
                val s = Array(7) { StateRef() }

                assertTransitions(
                    s[0], s[6],
                    mapOf(
                        s[0] to listOf(read('s', "") to s[1]),
                        s[1] to listOf(
                            rollup("", "s", "S") to s[2],
                            read('a', "s") to s[3]
                        ),
                        s[3] to listOf(rollup("", "sa", "A") to s[4]),
                        s[4] to listOf(rollup("\$", "A", "S") to s[2]),
                        s[2] to listOf(
                            exit("\$S") to s[6],
                            read('c', "\$S") to s[5]
                        ),
                        s[5] to listOf(rollup("", "Sc", "S") to s[2]),
                        s[6] to listOf()
                    )
                )
            }
        }
    }
}