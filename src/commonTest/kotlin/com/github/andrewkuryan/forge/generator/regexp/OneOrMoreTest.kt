package com.github.andrewkuryan.forge.generator.regexp

import kotlin.test.Test
import com.github.andrewkuryan.BNF.Grammar.Companion.S
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.utils.*

class OneOrMoreTest : GrammarTest({ buildRDParser() }) {

    @Test
    fun `should build NSA for OneOrMore❨Row❩`() =
        assertBuilding(grammar { S /= regexp { "def".oneOrMore() } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(read("def", "", "⁅(def)+⁆") to s[1]),
                    s[1] to listOf(
                        read("def", "") to s[1],
                        rollup("", "⁅(def)+⁆", "S") to s[2]
                    ),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for OneOrMore❨Or❨Symbol，Row，Range❩❩`() =
        assertBuilding(grammar { S /= regexp { ('a' / "bc" / ('g'..'i')).oneOrMore() } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(
                        read('a', "", "⁅(a|bc|[g-i])+⁆") to s[1],
                        read("bc", "", "⁅(a|bc|[g-i])+⁆") to s[1],
                        read("g-i", "", "⁅(a|bc|[g-i])+⁆") to s[1]
                    ),
                    s[1] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read("g-i", "") to s[1],
                        rollup("", "⁅(a|bc|[g-i])+⁆", "S") to s[2]
                    ),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for OneOrMore❨Or❨Symbol，Row，OneOrMore❨Symbol❩❩❩`() =
        assertBuilding(grammar { S /= regexp { ('a' / "bc" / 'f'.oneOrMore()).oneOrMore() } }) { s ->
            NSAAssertion(
                s[0], s[5],
                mapOf(
                    s[0] to listOf(
                        read('a', "", "⁅(a|bc|f+)+⁆") to s[1],
                        read("bc", "", "⁅(a|bc|f+)+⁆") to s[1],
                        read('f', "", "⁅(a|bc|f+)+⁆") to s[2]
                    ),
                    s[1] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read('f', "") to s[3],
                        rollup("", "⁅(a|bc|f+)+⁆", "S") to s[4]
                    ),
                    s[2] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read('f', "") to s[2],
                        read('f', "") to s[3],
                        rollup("", "⁅(a|bc|f+)+⁆", "S") to s[4]
                    ),
                    s[3] to listOf(
                        read('a', "") to s[1],
                        read("bc", "") to s[1],
                        read('f', "") to s[3],
                        rollup("", "⁅(a|bc|f+)+⁆", "S") to s[4]
                    ),
                    s[4] to listOf(exit("S$") to s[5]),
                    s[5] to listOf()
                )
            )
        }
}