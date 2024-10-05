package com.github.andrewkuryan.forge.generator.BNFtoRD.regular

import com.github.andrewkuryan.forge.generator.buildRDParser
import com.github.andrewkuryan.forge.generator.regular.LeftRecTest
import com.github.andrewkuryan.forge.utils.NSAAssertion
import com.github.andrewkuryan.forge.utils.exit
import com.github.andrewkuryan.forge.utils.read
import com.github.andrewkuryan.forge.utils.rollup
import kotlin.test.Test

class RDLeftRecTest : LeftRecTest({ buildRDParser() }) {

    @Test
    override fun `should build NSA for S → Sa ⏐ b`() =
        assertBuilding(`S → Sa ⏐ b`) { s ->
            NSAAssertion(
                s[0], s[4],
                mapOf(
                    s[0] to listOf(read('b', "") to s[1]),
                    s[1] to listOf(rollup("", "b", "S") to s[2]),
                    s[2] to listOf(
                        read('a', "") to s[3],
                        exit("\$S") to s[4]
                    ),
                    s[3] to listOf(rollup("", "Sa", "S") to s[2]),
                    s[4] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → Aa；A → Bb；B → Sc ⏐ d`() =
        assertBuilding(`S → Aa；A → Bb；B → Sc ⏐ d`) { s ->
            NSAAssertion(
                s[0], s[8],
                mapOf(
                    s[0] to listOf(read('d', "") to s[1]),
                    s[1] to listOf(rollup("", "d", "B") to s[2]),
                    s[2] to listOf(read('b', "") to s[3]),
                    s[3] to listOf(rollup("", "Bb", "A") to s[4]),
                    s[4] to listOf(read('a', "") to s[5]),
                    s[5] to listOf(rollup("", "Aa", "S") to s[6]),
                    s[6] to listOf(
                        read('c', "") to s[7],
                        exit("\$S") to s[8]
                    ),
                    s[7] to listOf(rollup("", "Sc", "B") to s[2]),
                    s[8] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → Sa ⏐ A；A → Bb；B → Sc ⏐ d`() =
        assertBuilding(`S → Sa ⏐ A；A → Bb；B → Sc ⏐ d`) { s ->
            NSAAssertion(
                s[0], s[8],
                mapOf(
                    s[0] to listOf(read('d', "") to s[1]),
                    s[1] to listOf(rollup("", "d", "B") to s[2]),
                    s[2] to listOf(read('b', "") to s[3]),
                    s[3] to listOf(rollup("", "Bb", "A") to s[4]),
                    s[4] to listOf(rollup("", "A", "S") to s[5]),
                    s[5] to listOf(
                        read('a', "") to s[6],
                        read('c', "") to s[7],
                        exit("\$S") to s[8]
                    ),
                    s[6] to listOf(rollup("", "Sa", "S") to s[5]),
                    s[7] to listOf(rollup("", "Sc", "B") to s[2]),
                    s[8] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → Ax ⏐ y；A → Sa ⏐ b`() =
        assertBuilding(`S → Ax ⏐ y；A → Sa ⏐ b`) { s ->
            NSAAssertion(
                s[0], s[7],
                mapOf(
                    s[0] to listOf(
                        read('y', "") to s[1],
                        read('b', "") to s[2]
                    ),
                    s[1] to listOf(rollup("", "y", "S") to s[3]),
                    s[3] to listOf(
                        read('a', "") to s[4],
                        exit("\$S") to s[7]
                    ),
                    s[4] to listOf(rollup("", "Sa", "A") to s[5]),
                    s[5] to listOf(read('x', "") to s[6]),
                    s[6] to listOf(rollup("", "Ax", "S") to s[3]),
                    s[2] to listOf(rollup("", "b", "A") to s[5]),
                    s[7] to listOf()
                )
            )
        }

    @Test
    override fun `should build NSA for S → Ai ⏐ s；A → Sa ⏐ Bb；B → Ac ⏐ d`() =
        assertBuilding(`S → Ai ⏐ s；A → Sa ⏐ Bb；B → Ac ⏐ d`) { s ->
            NSAAssertion(
                s[0], s[10],
                mapOf(
                    s[0] to listOf(
                        read('s', "") to s[1],
                        read('d', "") to s[2]
                    ),
                    s[1] to listOf(rollup("", "s", "S") to s[3]),
                    s[3] to listOf(
                        read('a', "") to s[4],
                        exit("\$S") to s[10]
                    ),
                    s[4] to listOf(rollup("", "Sa", "A") to s[5]),
                    s[5] to listOf(
                        read('i', "") to s[6],
                        read('c', "") to s[7]
                    ),
                    s[6] to listOf(rollup("", "Ai", "S") to s[3]),
                    s[7] to listOf(rollup("", "Ac", "B") to s[8]),
                    s[8] to listOf(read('b', "") to s[9]),
                    s[9] to listOf(rollup("", "Bb", "A") to s[5]),
                    s[2] to listOf(rollup("", "d", "B") to s[8]),
                    s[10] to listOf()
                )
            )
        }
}