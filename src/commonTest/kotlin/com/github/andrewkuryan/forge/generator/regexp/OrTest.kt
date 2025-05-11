package com.github.andrewkuryan.forge.generator.regexp

import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.utils.NSAAssertion
import com.github.andrewkuryan.forge.utils.assertDFABuilding
import com.github.andrewkuryan.forge.utils.read
import kotlin.test.Test

class OrTest {
    @Test
    fun `should build DFA for Or❨Row，Range❩`() =
        assertDFABuilding(regexp { "abc" / ('x'..'z') }) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(
                        read("abc", "") to s[1],
                        read("x-z", "") to s[1]
                    )
                )
            )
        }

    @Test
    fun `should build DFA for Or❨Symbol，Row，Row，Range❩`() =
        assertDFABuilding(regexp { 'e' / "sin" / "cos" / ('a'..'c') }) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(
                        read('e', "") to s[1],
                        read("sin", "") to s[1],
                        read("cos", "") to s[1],
                        read("a-c", "") to s[1]
                    )
                )
            )
        }

    @Test
    fun `should build DFA for Or❨Row，OneOrMore❨Range❩，Symbol，OneOrMore❨Row❩❩`() =
        assertDFABuilding(regexp { "ghi" / ('0'..'3').oneOrMore() / '5' / "test".oneOrMore() }) { s ->
            NSAAssertion(
                s[0], listOf(s[1], s[2], s[3]),
                mapOf(
                    s[0] to listOf(
                        read("ghi", "") to s[1],
                        read('5', "") to s[1],
                        read("0-3", "") to s[2],
                        read("test", "") to s[3]
                    ),
                    s[2] to listOf(read("0-3", "") to s[2]),
                    s[3] to listOf(read("test", "") to s[3])
                )
            )
        }

    @Test
    fun `should build DFA for Or❨Row，OneOrMore❨Or❨Row，Symbol❩❩，Range❩`() =
        assertDFABuilding(regexp { "test" / ("ab" / '*').oneOrMore() / ('0'..'9') }) { s ->
            NSAAssertion(
                s[0], listOf(s[1], s[2]),
                mapOf(
                    s[0] to listOf(
                        read("test", "") to s[1],
                        read("0-9", "") to s[1],
                        read("ab", "") to s[2],
                        read('*', "") to s[2]
                    ),
                    s[1] to listOf(),
                    s[2] to listOf(
                        read("ab", "") to s[2],
                        read('*', "") to s[2]
                    )
                )
            )
        }
}