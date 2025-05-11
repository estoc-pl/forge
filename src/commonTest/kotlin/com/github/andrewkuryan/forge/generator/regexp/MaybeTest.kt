package com.github.andrewkuryan.forge.generator.regexp

import com.github.andrewkuryan.BNF.RegExp
import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.utils.NSAAssertion
import com.github.andrewkuryan.forge.utils.assertDFABuilding
import com.github.andrewkuryan.forge.utils.read
import kotlin.test.Test

class MaybeTest {
    @Test
    fun `should build DFA for ε`() =
        assertDFABuilding(regexp("")) { s ->
            NSAAssertion(
                s[0], s[0],
                mapOf(s[0] to listOf())
            )
        }

    @Test
    fun `should build DFA for Maybe❨Range❩`() =
        assertDFABuilding(regexp { ('a'..'z').maybe() }) { s ->
            NSAAssertion(
                s[0], listOf(s[0], s[1]),
                mapOf(
                    s[0] to listOf(read("a-z", "") to s[1])
                )
            )
        }

    @Test
    fun `should build DFA for Maybe❨OneOrMore❨Row❩❩`() =
        assertDFABuilding(regexp { "klm".oneOrMore().maybe() }) { s ->
            NSAAssertion(
                s[0], s[0],
                mapOf(
                    s[0] to listOf(read("klm", "") to s[0])
                )
            )
        }

    @Test
    fun `should build DFA for Maybe❨Or❨Row，Symbol，Range❩❩`() =
        assertDFABuilding(regexp { "test" / 'p' / ('0'..'3') / RegExp.ε }) { s ->
            NSAAssertion(
                s[0], listOf(s[0], s[1]),
                mapOf(
                    s[0] to listOf(
                        read("test", "") to s[1],
                        read('p', "") to s[1],
                        read("0-3", "") to s[1]
                    )
                )
            )
        }

    @Test
    fun `should build DFA for Maybe❨OneOrMore❨Or❨Symbol，Symbol❩❩❩`() =
        assertDFABuilding(regexp { ('a' / 'b').oneOrMore().maybe() }) { s ->
            NSAAssertion(
                s[0], s[0],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[0],
                        read('b', "") to s[0],
                    )
                )
            )
        }

    @Test
    fun `should build DFA for Maybe❨Or❨Not，Symbol，OneOrMore❨Range❩❩❩`() =
        assertDFABuilding(regexp { (!'.' / 'k' / ('a'..'d').oneOrMore()).maybe() }) { s ->
            NSAAssertion(
                s[0], listOf(s[0], s[1], s[2]),
                mapOf(
                    s[0] to listOf(
                        read("^.", "") to s[1],
                        read('k', "") to s[1],
                        read("a-d", "") to s[2],
                    ),
                    s[1] to listOf(),
                    s[2] to listOf(read("a-d", "") to s[2])
                )
            )
        }
}