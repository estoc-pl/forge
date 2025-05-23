package com.github.andrewkuryan.forge.generator.BNFtoNSA

import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.forge.automata.StackSignal.Bottom
import com.github.andrewkuryan.forge.automata.StackSignal.NodeView
import com.github.andrewkuryan.forge.automata.StackSignal.Symbol
import com.github.andrewkuryan.forge.generator.collectPrefixes
import com.github.andrewkuryan.forge.generator.resolvePrefixes
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PrefixTest {

    @Test
    fun `should resolve prefixes for S → aA；A → bB ⏐ c；B → dA`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= 'a'..A
            A /= 'b'..B / 'c'
            B /= 'd'..A

            val prefixes = collectPrefixes()
            val sPrefixes = resolvePrefixes(S, prefixes)
            val aPrefixes = resolvePrefixes(A, prefixes)
            val bPrefixes = resolvePrefixes(B, prefixes)

            assertEquals(1, sPrefixes.size)
            assertEquals(3, aPrefixes.size)
            assertEquals(2, bPrefixes.size)

            assertContains(sPrefixes, listOf(Bottom))

            assertContains(aPrefixes, listOf(Bottom, Symbol('a')))
            assertContains(aPrefixes, listOf(Bottom, Symbol('a'), Symbol('b'), Symbol('d')))
            assertContains(aPrefixes, listOf(Symbol('b'), Symbol('d')))

            assertContains(bPrefixes, listOf(Bottom, Symbol('a'), Symbol('b')))
            assertContains(bPrefixes, listOf(Symbol('d'), Symbol('b')))
        }
    }

    @Test
    fun `should resolve prefixes for S → Aa；A → Bb ⏐ c；B → Ad`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= A..'a'
            A /= B..'b' / 'c'
            B /= A..'d'

            val prefixes = collectPrefixes()
            val sPrefixes = resolvePrefixes(S, prefixes)
            val aPrefixes = resolvePrefixes(A, prefixes)
            val bPrefixes = resolvePrefixes(B, prefixes)

            assertEquals(setOf(listOf(Bottom)), sPrefixes)
            assertEquals(setOf(listOf(Bottom)), aPrefixes)
            assertEquals(setOf(listOf(Bottom)), bPrefixes)
        }
    }

    @Test
    fun `should resolve prefixes for S → aAb；A → iSj ⏐ c`() {
        grammar {
            val A by nonterm()

            S /= 'a'..A..'b'
            A /= 'i'..S..'j' / 'c'

            val prefixes = collectPrefixes()
            val sPrefixes = resolvePrefixes(S, prefixes)
            val aPrefixes = resolvePrefixes(A, prefixes)

            assertEquals(3, sPrefixes.size)
            assertEquals(2, aPrefixes.size)

            assertContains(sPrefixes, listOf(Bottom))
            assertContains(sPrefixes, listOf(Bottom, Symbol('a'), Symbol('i')))
            assertContains(sPrefixes, listOf(Symbol('a'), Symbol('i')))

            assertContains(aPrefixes, listOf(Bottom, Symbol('a')))
            assertContains(aPrefixes, listOf(Symbol('i'), Symbol('a')))
        }
    }

    @Test
    fun `should resolve prefixes for S → aA；A → Bb；B → Sc`() {
        grammar {
            val A by nonterm()
            val B by nonterm()

            S /= 'a'..A
            A /= B..'b'
            B /= S..'c'

            val prefixes = collectPrefixes()
            val sPrefixes = resolvePrefixes(S, prefixes)
            val aPrefixes = resolvePrefixes(A, prefixes)
            val bPrefixes = resolvePrefixes(B, prefixes)

            assertEquals(3, sPrefixes.size)
            assertEquals(2, aPrefixes.size)
            assertEquals(2, bPrefixes.size)

            assertContains(sPrefixes, listOf(Bottom))
            assertContains(sPrefixes, listOf(Bottom, Symbol('a')))
            assertContains(sPrefixes, listOf(Symbol('a')))

            assertContains(aPrefixes, listOf(Bottom, Symbol('a')))
            assertContains(aPrefixes, listOf(Symbol('a')))

            assertContains(bPrefixes, listOf(Bottom, Symbol('a')))
            assertContains(bPrefixes, listOf(Symbol('a')))
        }
    }

    @Test
    fun `should resolve prefixes for S → SaA ⏐ A；A → Ab ⏐ b`() {
        grammar {
            val A by nonterm()

            S /= S..'a'..A / A
            A /= A..'b' / 'b'

            val prefixes = collectPrefixes()

            val sPrefixes = resolvePrefixes(S, prefixes)
            val aPrefixes = resolvePrefixes(A, prefixes)

            assertEquals(setOf(listOf(Bottom)), sPrefixes)

            assertEquals(2, aPrefixes.size)
            assertContains(aPrefixes, listOf(Bottom, NodeView("S"), Symbol('a')))
            assertContains(aPrefixes, listOf(Bottom))
        }
    }

    @Test
    fun `should resolve prefixes for S → SaA ⏐ A；A → bA ⏐ b`() {
        grammar {
            val A by nonterm()

            S /= S..'a'..A / A
            A /= 'b'..A / 'b'

            val prefixes = collectPrefixes()

            val sPrefixes = resolvePrefixes(S, prefixes)
            val aPrefixes = resolvePrefixes(A, prefixes)

            assertEquals(setOf(listOf(Bottom)), sPrefixes)

            assertEquals(5, aPrefixes.size)
            assertContains(aPrefixes, listOf(Bottom, NodeView("S"), Symbol('a')))
            assertContains(aPrefixes, listOf(Bottom))
            assertContains(aPrefixes, listOf(Bottom, NodeView("S"), Symbol('a'), Symbol('b')))
            assertContains(aPrefixes, listOf(Bottom, Symbol('b')))
            assertContains(aPrefixes, listOf(Symbol('b')))
        }
    }

    @Test
    fun `should resolve prefixes for S → AaS ⏐ A；A → bA ⏐ b`() {
        grammar {
            val A by nonterm()

            S /= A..'a'..S / A
            A /= 'b'..A / 'b'

            val prefixes = collectPrefixes()

            val sPrefixes = resolvePrefixes(S, prefixes)
            val aPrefixes = resolvePrefixes(A, prefixes)

            assertEquals(3, sPrefixes.size)
            assertEquals(7, aPrefixes.size)

            assertContains(sPrefixes, listOf(Bottom))
            assertContains(sPrefixes, listOf(Bottom, NodeView("A"), Symbol('a')))
            assertContains(sPrefixes, listOf(NodeView("A"), Symbol('a')))

            assertContains(aPrefixes, listOf(Bottom))
            assertContains(aPrefixes, listOf(Bottom, NodeView("A"), Symbol('a')))
            assertContains(aPrefixes, listOf(NodeView("A"), Symbol('a')))
            assertContains(aPrefixes, listOf(Bottom, Symbol('b')))
            assertContains(aPrefixes, listOf(Bottom, NodeView("A"), Symbol('a'), Symbol('b')))
            assertContains(aPrefixes, listOf(NodeView("A"), Symbol('a'), Symbol('b')))
            assertContains(aPrefixes, listOf(Symbol('b')))
        }
    }
}