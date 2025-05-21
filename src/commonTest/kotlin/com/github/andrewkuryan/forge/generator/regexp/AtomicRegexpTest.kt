package com.github.andrewkuryan.forge.generator.regexp

import kotlin.test.Test
import com.github.andrewkuryan.BNF.grammar
import com.github.andrewkuryan.BNF.regexp
import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.utils.*
import com.github.andrewkuryan.BNF.Grammar.Companion.S

class AtomicRegexpTest : GrammarTest({ buildRDParser() }) {

    @Test
    fun `should build NSA for Symbol`() =
        assertBuilding(grammar { S /= regexp('a') }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(read('a', "", "⁅a⁆") to s[1]),
                    s[1] to listOf(rollup("", "⁅a⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Row`() =
        assertBuilding(grammar { S /= regexp("abc") }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(read("abc", "", "⁅abc⁆") to s[1]),
                    s[1] to listOf(rollup("", "⁅abc⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Range`() =
        assertBuilding(grammar { S /= regexp('0'..'9') }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(read("0-9", "", "⁅[0-9]⁆") to s[1]),
                    s[1] to listOf(rollup("", "⁅[0-9]⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Simple Not`() =
        assertBuilding(grammar { S /= regexp { !'p' } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(read("^p", "", "⁅[^p]⁆") to s[1]),
                    s[1] to listOf(rollup("", "⁅[^p]⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    fun `should build NSA for Complex Not`() =
        assertBuilding(grammar { S /= regexp { !'k' * !('a'..'c') * !'f' * !'d' * !('x'..'z') } }) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(read("^[ka-cfdx-z]", "", "⁅[^ka-cfdx-z]⁆") to s[1]),
                    s[1] to listOf(rollup("", "⁅[^ka-cfdx-z]⁆", "S") to s[2]),
                    s[2] to listOf(exit("S$") to s[3]),
                    s[3] to listOf()
                )
            )
        }
}