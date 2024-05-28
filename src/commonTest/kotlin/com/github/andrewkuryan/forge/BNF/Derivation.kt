package com.github.andrewkuryan.forge.BNF

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.ProductionKind.Recursion
import com.github.andrewkuryan.forge.BNF.ProductionKind.Regular
import com.github.andrewkuryan.forge.BNF.RecursionKind.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DerivationTest {
    @Test
    fun `should return derivations for S → A ⏐ a；A → B ⏐ b；B → C ⏐ c；C → d`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A / 'a'
            A /= B / 'b'
            B /= C / 'c'
            C /= 'd'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                grouped, mapOf<Nonterminal, Map<ProductionKind, Set<Production>>>(
                    C to mapOf(Regular to setOf(listOf(Terminal('d')))),
                    B to mapOf(Regular to ('c' / 'd')),
                    A to mapOf(Regular to ('b' / 'c' / 'd')),
                    S to mapOf(Regular to ('a' / 'b' / 'c' / 'd'))
                )
            )
        }
    }

    @Test
    fun `should return derivations for S → aT；T → Sb ⏐ c`() {
        grammar {
            val T by nonterm()

            S /= 'a'..T
            T /= S..'b' / 'c'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                grouped, mapOf(
                    T to mapOf(Regular to (S..'b' / 'c')),
                    S to mapOf(
                        Recursion(setOf(CENTRAL)) to ('a'..S..'b'),
                        Regular to setOf(listOf(Terminal('a'), Terminal('c')))
                    )
                )
            )
        }
    }

    @Test
    fun `should return derivations for S → Aa ⏐ x；A → Bb ⏐ y；B → Cc ⏐ z；C → Ad ⏐ w`() {
        grammar {
            val A by nonterm()
            val B by nonterm()
            val C by nonterm()

            S /= A..'a' / 'x'
            A /= B..'b' / 'y'
            B /= C..'c' / 'z'
            C /= A..'d' / 'w'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                grouped, mapOf(
                    C to mapOf(Regular to (A..'d' / 'w')),
                    B to mapOf(Regular to (A..'d'..'c' / 'w'..'c' / 'z')),
                    A to mapOf(
                        Recursion(setOf(LEFT)) to (A..'d'..'c'..'b'),
                        Regular to ('w'..'c'..'b' / 'z'..'b' / 'y'),
                    ),
                    S to mapOf(Regular to (A..'d'..'c'..'b'..'a' / 'w'..'c'..'b'..'a' / 'z'..'b'..'a' / 'y'..'a' / 'x'))
                )
            )
        }
    }

    @Test
    fun `should return derivations for S → ASB；A → Sa ⏐ x；B → bS ⏐ y`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A..S..B
            A /= S..'a' / 'x'
            B /= 'b'..S / 'y'

            val grouped = getGroupedDerivations().mapValues { (_, value) ->
                value.mapValues { (_, nodes) -> nodes.map { it.getExpandedProduction() }.toSet() }
            }

            assertEquals(
                grouped, mapOf(
                    A to mapOf(Regular to (S..'a' / 'x')),
                    B to mapOf(Regular to ('b'..S / 'y')),
                    S to mapOf(
                        Recursion(setOf(LEFT, CENTRAL, RIGHT)) to (S..'a'..S..'b'..S),
                        Recursion(setOf(LEFT, CENTRAL)) to (S..'a'..S..'y'),
                        Recursion(setOf(CENTRAL, RIGHT)) to ('x'..S..'b'..S),
                        Recursion(setOf(CENTRAL)) to ('x'..S..'y')
                    )
                )
            )
        }
    }
}