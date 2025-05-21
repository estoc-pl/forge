package com.github.andrewkuryan.forge.generator.regexp

import kotlin.test.Test
import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.RegExp
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.utils.*

class MaybeTest : GrammarTest({ buildRDParser() }) {

    @Test
    fun `should build NSA for ε`() =
        assertBuilding(grammar { S /= regexp("") }) { s ->
            NSAAssertion(
                s[0], s[2],
                mapOf(
                    s[0] to listOf(rollup("", "", "S") to s[1]),
                    s[1] to listOf(exit("S$") to s[2]),
                    s[2] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Maybe❨Range❩`() =
        assertBuilding(grammar { S /= regexp { ('a'..'z').maybe() } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read("a-z", "", "⁅[a-z]⁆") to s[1],
                        rollup("", "", "S") to s[2],
                    ),
                    s[1] to listOf(rollup("", "⁅[a-z]⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Maybe❨OneOrMore❨Row❩❩`() =
        assertBuilding(grammar { S /= regexp { "klm".oneOrMore().maybe() } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read("klm", "", "⁅(klm)+⁆") to s[1],
                        rollup("", "", "S") to s[2]
                    ),
                    s[1] to listOf(
                        read("klm", "") to s[1],
                        rollup("", "⁅(klm)+⁆", "S") to s[2]
                    ),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Maybe❨Or❨Row，Symbol，Range❩❩`() =
        assertBuilding(grammar { S /= regexp { "test" / 'p' / ('0'..'3') / RegExp.ε } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read("test", "", "⁅(test|p|[0-3])⁆") to s[1],
                        read('p', "", "⁅(test|p|[0-3])⁆") to s[1],
                        read("0-3", "", "⁅(test|p|[0-3])⁆") to s[1],
                        rollup("", "", "S") to s[2]
                    ),
                    s[1] to listOf(rollup("", "⁅(test|p|[0-3])⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Maybe❨OneOrMore❨Or❨Symbol，Symbol❩❩❩`() =
        assertBuilding(grammar { S /= regexp { ('a' / 'b').oneOrMore().maybe() } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read('a', "", "⁅[ab]+⁆") to s[1],
                        read('b', "", "⁅[ab]+⁆") to s[1],
                        rollup("", "", "S") to s[2]
                    ),
                    s[1] to listOf(
                        read('a', "") to s[1],
                        read('b', "") to s[1],
                        rollup("", "⁅[ab]+⁆", "S") to s[2]
                    ),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Maybe❨Or❨Not，Symbol，OneOrMore❨Range❩❩❩`() =
        assertBuilding(grammar { S /= regexp { (!'.' / 'k' / ('a'..'d').oneOrMore()).maybe() } }) { s ->
            NSAAssertion(
                s[0], s[4],
                mapOf(
                    s[0] to listOf(
                        read("^.", "", "⁅([^.]|k|[a-d]+)⁆") to s[1],
                        read('k', "", "⁅([^.]|k|[a-d]+)⁆") to s[1],
                        read("a-d", "", "⁅([^.]|k|[a-d]+)⁆") to s[2],
                        rollup("", "", "S") to s[3]
                    ),
                    s[1] to listOf(rollup("", "⁅([^.]|k|[a-d]+)⁆", "S") to s[3]),
                    s[2] to listOf(
                        read("a-d", "") to s[2],
                        rollup("", "⁅([^.]|k|[a-d]+)⁆", "S") to s[3]
                    ),
                    s[3] to listOf(exit("S$") to s[4]),
                    s[4] to listOf()
                )
            )
        }
}