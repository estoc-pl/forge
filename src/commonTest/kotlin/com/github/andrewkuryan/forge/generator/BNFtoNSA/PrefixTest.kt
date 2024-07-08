package com.github.andrewkuryan.forge.generator.BNFtoNSA

import com.github.andrewkuryan.forge.BNF.Grammar.Companion.S
import com.github.andrewkuryan.forge.BNF.grammar
import com.github.andrewkuryan.forge.automata.StackSignal.Bottom
import com.github.andrewkuryan.forge.automata.StackSignal.Letter
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

            assertContains(aPrefixes, listOf(Bottom, Letter("a")))
            assertContains(aPrefixes, listOf(Bottom, Letter("a"), Letter("b"), Letter("d")))
            assertContains(aPrefixes, listOf(Letter("b"), Letter("d")))

            assertContains(bPrefixes, listOf(Bottom, Letter("a"), Letter("b")))
            assertContains(bPrefixes, listOf(Letter("d"), Letter("b")))
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

            assertEquals(sPrefixes, setOf(listOf(Bottom)))
            assertEquals(aPrefixes, setOf(listOf(Bottom)))
            assertEquals(bPrefixes, setOf(listOf(Bottom)))
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
            assertContains(sPrefixes, listOf(Bottom, Letter("a"), Letter("i")))
            assertContains(sPrefixes, listOf(Letter("a"), Letter("i")))

            assertContains(aPrefixes, listOf(Bottom, Letter("a")))
            assertContains(aPrefixes, listOf(Letter("i"), Letter("a")))
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

            assertEquals(sPrefixes, setOf(listOf(Bottom)))

            assertEquals(2, aPrefixes.size)
            assertContains(aPrefixes, listOf(Bottom, Letter("S"), Letter("a")))
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

            assertEquals(sPrefixes, setOf(listOf(Bottom)))

            assertEquals(5, aPrefixes.size)
            assertContains(aPrefixes, listOf(Bottom, Letter("S"), Letter("a")))
            assertContains(aPrefixes, listOf(Bottom))
            assertContains(aPrefixes, listOf(Bottom, Letter("S"), Letter("a"), Letter("b")))
            assertContains(aPrefixes, listOf(Bottom, Letter("b")))
            assertContains(aPrefixes, listOf(Letter("b")))
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
            assertContains(sPrefixes, listOf(Bottom, Letter("A"), Letter("a")))
            assertContains(sPrefixes, listOf(Letter("A"), Letter("a")))

            assertContains(aPrefixes, listOf(Bottom))
            assertContains(aPrefixes, listOf(Bottom, Letter("A"), Letter("a")))
            assertContains(aPrefixes, listOf(Letter("A"), Letter("a")))
            assertContains(aPrefixes, listOf(Bottom, Letter("b")))
            assertContains(aPrefixes, listOf(Bottom, Letter("A"), Letter("a"), Letter("b")))
            assertContains(aPrefixes, listOf(Letter("A"), Letter("a"), Letter("b")))
            assertContains(aPrefixes, listOf(Letter("b")))
        }
    }
}