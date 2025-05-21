package com.github.andrewkuryan.forge.generator.regexp

import kotlin.test.Test
import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.utils.*

class OrTest : GrammarTest({ buildRDParser() }) {

    @Test
    fun `should build NSA for Or❨Row，Range❩`() =
        assertBuilding(grammar { S /= regexp { "abc" / ('x'..'z') } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read("abc", "", "⁅(abc|[x-z])⁆") to s[1],
                        read("x-z", "", "⁅(abc|[x-z])⁆") to s[1]
                    ),
                    s[1] to listOf(rollup("", "⁅(abc|[x-z])⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Or❨Symbol，Row，Row，Range❩`() =
        assertBuilding(grammar { S /= regexp { 'e' / "sin" / "cos" / ('a'..'c') } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read('e', "", "⁅(e|sin|cos|[a-c])⁆") to s[1],
                        read("sin", "", "⁅(e|sin|cos|[a-c])⁆") to s[1],
                        read("cos", "", "⁅(e|sin|cos|[a-c])⁆") to s[1],
                        read("a-c", "", "⁅(e|sin|cos|[a-c])⁆") to s[1]
                    ),
                    s[1] to listOf(rollup("", "⁅(e|sin|cos|[a-c])⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Or❨Row，OneOrMore❨Range❩，Symbol，OneOrMore❨Row❩❩`() =
        assertBuilding(grammar { S /= regexp { "ghi" / ('0'..'3').oneOrMore() / '5' / "test".oneOrMore() } }) { s ->
            NSAAssertion(
                s[0], s[5],
                mapOf(
                    s[0] to listOf(
                        read("ghi", "", "⁅(ghi|[0-3]+|5|(test)+)⁆") to s[1],
                        read('5', "", "⁅(ghi|[0-3]+|5|(test)+)⁆") to s[1],
                        read("0-3", "", "⁅(ghi|[0-3]+|5|(test)+)⁆") to s[2],
                        read("test", "", "⁅(ghi|[0-3]+|5|(test)+)⁆") to s[3]
                    ),
                    s[1] to listOf(rollup("", "⁅(ghi|[0-3]+|5|(test)+)⁆", "S") to s[4]),
                    s[2] to listOf(
                        read("0-3", "") to s[2],
                        rollup("", "⁅(ghi|[0-3]+|5|(test)+)⁆", "S") to s[4]
                    ),
                    s[3] to listOf(
                        read("test", "") to s[3],
                        rollup("", "⁅(ghi|[0-3]+|5|(test)+)⁆", "S") to s[4]
                    ),
                    s[4] to listOf(exit("S$") to s[5]),
                    s[5] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Or❨Row，OneOrMore❨Or❨Row，Symbol❩❩，Range❩`() =
        assertBuilding(grammar { S /= regexp { "test" / ("ab" / '*').oneOrMore() / ('0'..'9') } }) { s ->
            NSAAssertion(
                s[0], s[4],
                mapOf(
                    s[0] to listOf(
                        read("test", "", "⁅(test|(ab|*)+|[0-9])⁆") to s[1],
                        read("0-9", "", "⁅(test|(ab|*)+|[0-9])⁆") to s[1],
                        read("ab", "", "⁅(test|(ab|*)+|[0-9])⁆") to s[2],
                        read('*', "", "⁅(test|(ab|*)+|[0-9])⁆") to s[2]
                    ),
                    s[1] to listOf(rollup("", "⁅(test|(ab|*)+|[0-9])⁆", "S") to s[3]),
                    s[2] to listOf(
                        read("ab", "") to s[2],
                        read('*', "") to s[2],
                        rollup("", "⁅(test|(ab|*)+|[0-9])⁆", "S") to s[3]
                    ),
                    s[3] to listOf(exit("S$") to s[4]),
                    s[4] to listOf()
                )
            )
        }
}