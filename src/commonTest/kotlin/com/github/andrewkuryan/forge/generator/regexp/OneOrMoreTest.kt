package com.github.andrewkuryan.forge.generator.regexp

import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.utils.NSAAssertion
import com.github.andrewkuryan.forge.utils.assertDFABuilding
import com.github.andrewkuryan.forge.utils.read
import kotlin.test.Test

class OneOrMoreTest {
    @Test
    fun `should build DFA for OneOrMore❨Row❩`() =
        assertDFABuilding(regexp { "def".oneOrMore() }) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(read("def", "") to s[1]),
                    s[1] to listOf(read("def", "") to s[1])
                )
            )
        }

    @Test
    fun `should build DFA for OneOrMore❨Or❨Symbol，Row，Range❩❩`() =
        assertDFABuilding(regexp { ('a' / "bc" / ('g'..'i')).oneOrMore() }) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read("g-i", "") to s[1]
                    ),
                    s[1] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read("g-i", "") to s[1]
                    )
                )
            )
        }

    @Test
    fun `should build DFA for OneOrMore❨Or❨Symbol，Row，OneOrMore❨Symbol❩❩❩`() =
        assertDFABuilding(regexp { ('a' / "bc" / 'f'.oneOrMore()).oneOrMore() }) { s ->
            NSAAssertion(
                s[0], s[1],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read('f', "") to s[1]
                    ),
                    s[1] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read('f', "") to s[1]
                    )
                )
            )
        }
}