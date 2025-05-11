package com.github.andrewkuryan.forge.generator.regexp

import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.utils.*
import kotlin.test.Test

class AtomicRegexpTest {
    @Test
    fun `should build DFA for Symbol`() =
        assertDFABuilding(regexp('a')) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(read('a', "") to s[1]),
                    s[1] to listOf()
                )
            )
        }

    @Test
    fun `should build DFA for Row`() =
        assertDFABuilding(regexp("abc")) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(read("abc", "") to s[1]),
                    s[1] to listOf()
                )
            )
        }

    @Test
    fun `should build DFA for Range`() =
        assertDFABuilding(regexp('0'..'9')) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(read("0-9", "") to s[1]),
                    s[1] to listOf()
                )
            )
        }

    @Test
    fun `should build DFA for Simple Not`() =
        assertDFABuilding(regexp { !'p' }) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(read("^p", "") to s[1]),
                    s[1] to listOf()
                )
            )
        }

    @Test
    fun `should build DFA for Complex Not`() =
        assertDFABuilding(regexp { !'k' * !('a'..'c') * !'f' * !'d' * !('x'..'z') }) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(read("^[ka-cfdx-z]", "") to s[1]),
                    s[1] to listOf()
                )
            )
        }
}