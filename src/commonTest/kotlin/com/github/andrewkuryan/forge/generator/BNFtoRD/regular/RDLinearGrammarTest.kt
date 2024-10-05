package com.github.andrewkuryan.forge.generator.BNFtoRD.regular

import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.generator.regular.LinearGrammarTest
import com.github.andrewkuryan.forge.utils.NSAAssertion
import com.github.andrewkuryan.forge.utils.exit
import com.github.andrewkuryan.forge.utils.read
import com.github.andrewkuryan.forge.utils.rollup
import kotlin.test.Test

class RDLinearGrammarTest : LinearGrammarTest({ buildRDParser() }) {

    @Test
    override fun `should build NSA for S → a`() =
        assertBuilding(`S → a`) { s ->
            NSAAssertion(
                s[0], s[3],
                mapOf(
                    s[0] to listOf(read('a', "") to s[1]),
                    s[1] to listOf(rollup("", "a", "S") to s[2]),
                    s[2] to listOf(exit("\$S") to s[3]),
                    s[3] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → As；A → aB；B → bC；C → cd`() =
        assertBuilding(`S → As；A → aB；B → bC；C → cd`) { s ->
            NSAAssertion(
                s[0], s[10],
                mapOf(
                    s[0] to listOf(read('a', "") to s[1]),
                    s[1] to listOf(read('b', "") to s[2]),
                    s[2] to listOf(read('c', "") to s[3]),
                    s[3] to listOf(read('d', "") to s[4]),
                    s[4] to listOf(rollup("", "cd", "C") to s[5]),
                    s[5] to listOf(rollup("", "bC", "B") to s[6]),
                    s[6] to listOf(rollup("", "aB", "A") to s[7]),
                    s[7] to listOf(read('s', "") to s[8]),
                    s[8] to listOf(rollup("", "As", "S") to s[9]),
                    s[9] to listOf(exit("\$S") to s[10]),
                    s[10] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → A ⏐ B；A → abc；B → de`() =
        assertBuilding(`S → A ⏐ B；A → abc；B → de`) { s ->
            NSAAssertion(
                s[0], s[9],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[1],
                        read('d', "") to s[6]
                    ),
                    s[1] to listOf(read('b', "") to s[2]),
                    s[2] to listOf(read('c', "") to s[3]),
                    s[3] to listOf(rollup("", "abc", "A") to s[4]),
                    s[4] to listOf(rollup("", "A", "S") to s[5]),
                    s[5] to listOf(exit("\$S") to s[9]),
                    s[6] to listOf(read('e', "") to s[7]),
                    s[7] to listOf(rollup("", "de", "B") to s[8]),
                    s[8] to listOf(rollup("", "B", "S") to s[5]),
                    s[9] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → A ⏐ B；A → aС；B → bC；C → c`() =
        assertBuilding(`S → A ⏐ B；A → aС；B → bC；C → c`) { s ->
            NSAAssertion(
                s[0], s[7],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[1],
                        read('a', "") to s[1]
                    ),
                    s[1] to listOf(read('c', "") to s[2]),
                    s[2] to listOf(rollup("", "c", "C") to s[3]),
                    s[3] to listOf(
                        rollup("", "aC", "A") to s[4],
                        rollup("", "bC", "B") to s[5]
                    ),
                    s[4] to listOf(rollup("", "A", "S") to s[6]),
                    s[5] to listOf(rollup("", "B", "S") to s[6]),
                    s[6] to listOf(exit("\$S") to s[7]),
                    s[7] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → aA ⏐ bB；A → Сc；B → Cd；C → e`() =
        assertBuilding(`S → aA ⏐ bB；A → Сc；B → Cd；C → e`) { s ->
            NSAAssertion(
                s[0], s[9],
                mapOf(
                    s[0] to listOf(
                        read('a', "") to s[1],
                        read('b', "") to s[1]
                    ),
                    s[1] to listOf(read('e', "") to s[2]),
                    s[2] to listOf(rollup("", "e", "C") to s[3]),
                    s[3] to listOf(
                        read('c', "") to s[4],
                        read('d', "") to s[5],
                    ),
                    s[4] to listOf(rollup("", "Cc", "A") to s[6]),
                    s[5] to listOf(rollup("", "Cd", "B") to s[7]),
                    s[6] to listOf(rollup("", "aA", "S") to s[8]),
                    s[7] to listOf(rollup("", "bB", "S") to s[8]),
                    s[8] to listOf(exit("\$S") to s[9]),
                    s[9] to listOf()
                )
            )
        }
}